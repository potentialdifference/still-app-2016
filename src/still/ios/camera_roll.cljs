(ns still.ios.camera-roll
  (:require [reagent.core :as r]))

(def ReactNative (js/require "react-native"))

(def camera-roll (.-CameraRoll ReactNative))

(defn fetch-camera-images
  [f]
  (-> (.getPhotos camera-roll (clj->js {:first 5 :groupTypes "SavedPhotos" :assetType "Photos"}))
      (.then f (fn [error] (js/console.log error)))))
