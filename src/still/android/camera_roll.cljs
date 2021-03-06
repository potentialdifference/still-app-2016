(ns still.android.camera-roll
  (:require [reagent.core :as r]
            [still.config :refer [config]]))

(def ReactNative (js/require "react-native"))
(def NativeModules (js/require "NativeModules"))
(def blob-uploader (.-default (js/require "react-native-fetch-blob")))
(def uploader (.-RNUploader NativeModules))
(def camera-roll (.-CameraRoll ReactNative))

(defn fetch-album [{:keys [on-success on-error]}]
  (-> (.getPhotos camera-roll (clj->js {:first 5 :assetType "Photos"}))
      (.then on-success on-error)))

(defn upload! [opts {:keys [on-success on-error]}]
  (js/console.log "uploading pic")
  (-> (.config blob-uploader (clj->js {:trusty true}))
      (.fetch "POST" (str (:url opts) "?tag=" (:tag opts) "&uid=" (:user-id opts))
                (clj->js {"Content-Type" "multipart/form-data" "Authorization" (:auth-token config)})
                (clj->js (:files opts)))
      (.then on-success on-error)))

(defn upload-assets! [{:keys [paths on-success on-error device-name]}]
  (js/console.log "upload assets called")
  (let [path->asset (fn [path]
                      {:name "images[]"
                       :filename (str (rand-int 10000) ".jpg")
                       :data (.wrap blob-uploader path)})
        opts {:url (:private-host config)
              :files (map path->asset paths)
              :tag "user-photo"
              :user-id device-name}]
    (upload! opts {:on-success on-success
                   :on-error on-error})))

(defn album-paths [{:keys [on-success on-error]}]
  (fetch-album
    {:on-error on-error
     :on-success (fn [data]
                   (on-success (->> (js->clj data :keywordize-keys true)
                                    (:edges)
                                    (map #(get-in % [:node :image :uri])))))}))
