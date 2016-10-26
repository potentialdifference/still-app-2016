(ns still.db
  (:require [clojure.spec :as s]))

;; spec of app-db
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::greeting ::camera-type]))

;; initial state of app-db
(def app-db {:greeting "Hello Clojure in iOS and Android!"
             :camera-type :back})
