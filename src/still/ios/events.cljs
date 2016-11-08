(ns still.ios.events
  (:require [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
            [still.ios.camera-roll :refer [upload-album!]]))


(reg-fx
 :upload-album!
 (fn [callback]
   (upload-album! callback)))
