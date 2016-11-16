(ns still.config)

(def ip
  "192.168.91.77")

(def dev
  {:private-host (str "https://" ip ":8443")
   :public-host (str "http://" ip ":8080")
   :ws-host (str "ws://" ip ":8080")
   :valid-ssids #{"Ovalcafe"}
   :default-ssid "FETCHING"
   :auth-token "j2GY21Djms5pqfH2"})

;; Prod config...
#_(def prod
  {:private-host "https://192.168.2.25:8443"
   :public-host "http://192.168.2.25:8080"
   :ws-host "ws://192.168.2.25:8080"
   :valid-ssids #{"VivianMaier"}
   :default-ssid "FETCHING"
   :auth-token "j2GY21Djms5pqfH2"})

(def config
  (merge dev {:upload-url (str (:private-host dev) "/private")}))
