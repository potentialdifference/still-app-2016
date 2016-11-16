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

(defn take-picture! []
  (.. Camera -CameraManager
      (capture (clj->js {:target (.. Camera -constants -CaptureTarget -cameraRoll)}))
      (then (fn [data]
              (js/console.log (str "got data " data))
              (let [asset (js->clj data :keywordize-keys true)]
                (js/console.log "Queuing for upload..." (:path asset))
                (dispatch [:queue-for-upload (:path asset)]))))))

(defn fetch-album [{:keys [query on-success on-error]}]
  (-> (.getPhotos camera-roll (clj->js query))
      (.then on-success on-error)))

(defn upload! [{:keys [url user-id files]} {:keys [on-success on-error]}]
  (-> (.config blob-uploader (clj->js {:trusty true}))
      (.fetch "POST" (str url "?uid=" user-id)
              (clj->js {"Content-Type" "multipart/form-data"
                        "Authorization" (:auth-token config)})
              (clj->js files))
      (.then on-success on-error))) ;; TODO

(defn upload-assets! [{:keys [assets on-success on-error device-name]}]
  ;; (js/alert "Uploading assets!")
  (let [path->asset (fn [{:keys [path tag]}]
                      {:name tag
                       :filename (str (rand-int 10000) ".jpg")
                       :data (.wrap blob-uploader path)})
        opts {:url (:upload-url config)
              :files (map path->asset assets)
              :user-id "device-name"}]
    (upload! opts {:on-success on-success
                   :on-error on-error})))

(defn myprint [arg]
  (doto arg println))

(defn album-paths [{:keys [query on-success on-error]}]
  (fetch-album
   {:query query
    :on-error on-error
    :on-success (fn [data]
                  (on-success (->> (js->clj data :keywordize-keys true)
                                   (:edges)
                                   (myprint)
                                   (map #(get-in % [:node :image :uri])))))}))

