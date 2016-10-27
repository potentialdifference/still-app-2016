(ns still.db
  (:require [clojure.spec :as s]))

;; spec of app-db
(s/def ::index integer?)
(s/def ::key keyword?)
(s/def ::title string?)

(s/def ::route (s/keys :req-un [::key
                                ::title]))
(s/def ::routes (s/* ::route))

(s/def ::nav (s/keys :req-un [::index
                              ::key
                              ::routes]))

(s/def ::app-db (s/keys :req-un [::nav ::message ::images]))

;; initial state of app-db
(defn app-db
  [route]
  {:message "Initial message"
   :images []
   :nav {:index 0
         :key :home
         :routes [route]}})
