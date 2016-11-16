(ns still.ios.events
  (:require [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
            [still.shared :refer [album-paths upload-assets!]]))

(reg-fx
 :queue-album-for-upload!
 (fn [_]
   (album-paths {:query {:first 5 :groupTypes "SavedPhotos" :assetType "Photos"}
                 :on-success (fn [paths]
                               (doseq [path paths]
                                 (dispatch [:queue-for-upload {:tag "other"
                                                               :path path}])))
                 :on-failure #(js/console.log "Error:" %)})))

