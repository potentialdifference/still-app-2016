(ns still.db
  (:require [clojure.spec :as s]))

;; spec of app-db
(s/def ::index integer?)
(s/def ::key keyword?)
(s/def ::title string?)
(s/def ::ssid string?)
(s/def ::album-queued? boolean?)
(s/def ::privacy-policy-agreed? boolean?)
(s/def ::camera-authorized? boolean?)
(s/def ::images-sent? boolean?)
(s/def ::device-name string?)
(s/def ::awaiting-show? boolean?)

(s/def ::path string?)
(s/def ::tag string?)

(s/def ::route (s/keys :req-un [::key
                                ::title]))
(s/def ::routes (s/* ::route))

(s/def ::nav (s/keys :req-un [::index
                              ::key
                              ::routes]))

(s/def ::photo (s/keys :req-un [::path
                                ::tag]))

(s/def ::upload-queue (s/* ::photo))

(s/def ::show (s/keys :opt-un [::image-uri ::message-content]))

(s/def ::app-db (s/keys :req-un [::nav ::message ::images ::ssid
                                 ::album-queued?
                                 ::privacy-policy-agreed?
                                 ::camera-authorized?
                                 ::upload-queue
                                 ::show
                                 ::device-name
                                 ::awaiting-show?]))

;; initial state of app-db
(defn app-db
  [route device-name]
  {:message "Initial message"
   :images []
   :nav {:index 0
         :key :home
         :routes [route]}
   :ssid "FETCHING"
   :album-queued? false
   :privacy-policy-agreed? false
   :camera-authorized? false
   :images-sent? false
   :upload-queue []
   :awaiting-show? true
   :show {}
   :device-name device-name})
