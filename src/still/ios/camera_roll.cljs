(ns still.ios.camera-roll
  (:require [reagent.core :as r]
            [still.config :refer [config]]))

(def ReactNative (js/require "react-native"))
(def NativeModules (js/require "NativeModules"))
(def uploader (.-RNUploader NativeModules))
(def camera-roll (.-CameraRoll ReactNative))

(defn fetch-album [{:keys [on-success on-error]}]
  (-> (.getPhotos camera-roll (clj->js {:first 5 :groupTypes "SavedPhotos" :assetType "Photos"}))
      (.then on-success on-error)))

(defn upload! [opts {:keys [on-success on-error]}]
  (.upload uploader (clj->js opts)
           (fn [error success]
             (cond error (on-error error)
                   success (on-success success)))))

(defn upload-assets! [{:keys [paths on-success on-error]}]
  (let [path->asset (fn [path]
                      {:name "images[]"
                       :filename (str (rand-int 10000) ".jpg")
                       :filepath path})
        opts {:url (:private-host config)
              :files (map path->asset paths)}]
    (upload! opts {:on-success #(println "Success!" %)
                   :on-error #(println "Error!" %)})))

(defn album-paths [{:keys [on-success on-error]}]
  (fetch-album
   {:on-error on-error
    :on-success (fn [data]
                  (on-success (->> (js->clj data :keywordize-keys true)
                                   (:edges)
                                   (map #(get-in % [:node :image :uri])))))}))
