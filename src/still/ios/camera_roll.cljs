(ns still.ios.camera-roll
  (:require [reagent.core :as r]
            [still.config :refer [config]]))

(def ReactNative (js/require "react-native"))
(def NativeModules (js/require "NativeModules"))
(def uploader (.-RNUploader NativeModules))
(def camera-roll (.-CameraRoll ReactNative))

(defn fetch-album
  [f]
  (-> (.getPhotos camera-roll (clj->js {:first 5 :groupTypes "SavedPhotos" :assetType "Photos"}))
      (.then f (fn [error] (js/console.log error)))))

(defn upload-album! [callback]
  (let [edge->file (fn [edge]
                     {:name "images[]"
                      :filename (get-in edge [:node :image :filename])
                      :filepath (get-in edge [:node :image :uri])})
        edges->files (partial map edge->file)]
    (fetch-album
     (fn fetch-callback [data]
       (let [files (edges->files (:edges (js->clj data :keywordize-keys true)))
             opts (clj->js {:url (:private-host config)
                            :files files})]
         (.upload uploader opts
                  (fn upload-callback [error response]
                    (when error
                      (js/console.log (str "upload error: " (js->clj error))))
                    (when response
                      (callback response)))))))))
