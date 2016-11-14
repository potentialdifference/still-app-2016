(ns still.config)

(def config
  {:private-host "https://10.0.1.2:8443/private"
   :public-host "http://10.0.1.2:8080"
   :ws-host "ws://10.0.1.2:8080"
   :valid-ssids #{"Alpaca"}
   :default-ssid "FETCHING"})
