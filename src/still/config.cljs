(ns still.config)

(def dev-ip
  "10.0.1.2")

(def prod-ip
  "192.168.2.25")

(def dev
  {:private-host (str "https://" dev-ip ":8443")
   :public-host (str "http://" dev-ip ":8080")
   :ws-host (str "ws://" dev-ip ":8080")
   :valid-ssids #{"roomie", "error" "Alpaca"}
   :default-ssid "FETCHING"
   :auth-token "j2GY21Djms5pqfH2"})

(def prod
  {:private-host (str "https://" prod-ip ":8443")
   :public-host (str "http://" prod-ip ":8080")
   :ws-host (str "ws://" prod-ip ":8080")
   :valid-ssids #{"VivianMaier"}
   :default-ssid "FETCHING"
   :auth-token "j2GY21Djms5pqfH2"})

(def env dev)

(def config
  (merge env {:upload-url (str (:private-host env) "/private")}))
