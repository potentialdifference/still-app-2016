(ns still.android.events
  (:require [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
            [still.shared :refer [album-paths upload-assets!]]))

(reg-fx
 :queue-album-for-upload!
 (fn [_]
   (album-paths {:query {:first 5 :assetType "Photos"}
                 :on-success (fn [paths]
                               (doseq [path paths]
                                 (dispatch [:queue-for-upload {:path path
                                                               :tag "other"}])))
                 :on-failure #(js/console.log "Error:" %)})))
