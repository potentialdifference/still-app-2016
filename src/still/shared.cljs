(ns still.shared
  (:require [re-frame.core :refer [dispatch subscribe]]
            [still.config :refer [config]]))

(def network-info (js/require "react-native-network-info"))
(def Camera (js/require "react-native-camera"))
(def ReactNative (js/require "react-native"))
(def NativeModules (js/require "NativeModules"))
(def blob-uploader (.-default (js/require "react-native-fetch-blob")))
(def uploader (.-RNUploader NativeModules))
(def camera-roll (.-CameraRoll ReactNative))

(defn fetch-ssid [callback]
  (.getSSID network-info callback))



(defn take-picture! [{:keys [target tag shutter? type callback]
                      :or   {target   :camera-roll
                             tag      "rear"
                             shutter? false
                             type :rear}}]
  (js/console.log (str "take pic " type))
  (let [target (case target
                 :camera-roll (.. Camera -constants -CaptureTarget -cameraRoll)
                 :disk (.. Camera -constants -CaptureTarget -disk))
                 :temp (.. Camera -constants -CaptureTarget -temp))
        type (case type
                  :front (.. Camera -constants -Type -front)
                  :rear (.. Camera -constants -Type -back))
        mode (.. Camera -constants -CaptureMode -still)]
    (.. Camera -CameraManager
        (capture (clj->js {:target target :playSoundOnCapture shutter? :type type :mode mode :quality "medium"}))
        (then (fn [data]
                (js/console.log (str "got data " data))
                (let [asset (js->clj data :keywordize-keys true)]
                  (js/console.log "Queuing for upload..." (:path asset))
                  (dispatch [:queue-for-upload {:path (:path asset)
                                                :tag tag}])
                  (when callback (callback)))))
        (catch (fn [_]
                 )))))

(defn fetch-album [{:keys [query on-success on-error]}]
  (-> (.getPhotos camera-roll (clj->js query))
      (.then on-success)
      (.catch on-error)))

(defn upload! [{:keys [url user-id files]} {:keys [on-success on-error]}]
  (-> (.config blob-uploader (clj->js {:trusty true}))
      (.fetch "POST" (str url "?uid=" user-id)
              (clj->js {"Content-Type" "multipart/form-data"
                        "Authorization" (:auth-token config)})
              (clj->js files))
      (.then on-success)
      (.catch on-error)))

(defn upload-assets! [{:keys [assets user-id on-success on-error device-name]}]
  (let [path->asset (fn [{:keys [path tag]}]
                      {:name tag
                       :filename (str (rand-int 10000) ".jpg")
                       :data (.wrap blob-uploader path)})
        opts {:url (:upload-url config)
              :files (map path->asset assets)
              :user-id (js/encodeURIComponent user-id)}]
    (upload! opts {:on-success (fn [response]
                                 (on-success assets))
                   :on-error on-error})))

(defn album-paths [{:keys [query on-success on-error]}]
  (fetch-album
   {:query query
    :on-error on-error
    :on-success (fn [data]
                  (on-success (->> (js->clj data :keywordize-keys true)
                                   (:edges)
                                   (map #(get-in % [:node :image :uri])))))}))

