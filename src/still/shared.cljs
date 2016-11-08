(ns still.shared)

(def network-info (js/require "react-native-network-info"))

(defn fetch-ssid [callback]
  (.getSSID network-info callback))
