(ns still.shared
  (:require [re-frame.core :refer [dispatch subscribe]]))

(def network-info (js/require "react-native-network-info"))
(def Camera (js/require "react-native-camera"))

(defn fetch-ssid [callback]
  (.getSSID network-info callback))

(defn take-picture! []
  (.. Camera -CameraManager
      (capture (clj->js {:target (.. Camera -constants -CaptureTarget -cameraRoll)}))
      (then (fn [data]
              (let [asset (js->clj data :keywordize-keys true)]
                (println "Queuing for upload..." (:path asset))
                (dispatch [:queue-for-upload (:path asset)]))))))
