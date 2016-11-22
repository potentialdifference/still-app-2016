(ns still.config)

(def dev-ip
  "192.168.0.1")

(def prod-ip
  "192.168.0.2")

(def dev
  {:private-host (str "https://" dev-ip ":8443")
   :public-host (str "http://" dev-ip ":8080")
   :ws-host (str "ws://" dev-ip ":8080")
   :valid-ssids #{"SAFE_SSID"}
   :default-ssid "FETCHING"
   :auth-token "XXXXXXXX"})

(def prod
  {:private-host (str "https://" prod-ip ":8443")
   :public-host (str "http://" prod-ip ":8080")
   :ws-host (str "ws://" prod-ip ":8080")
   :valid-ssids #{"SAFE_SSID"}
   :default-ssid "FETCHING"
   :auth-token "YYYYYYYY"})

(def env prod)

(def config
  (merge env {:upload-url (str (:private-host env) "/private")}))
