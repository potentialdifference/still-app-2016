(ns still.android.events
  (:require [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
            [still.android.camera-roll :refer [album-paths upload-assets!]]))

(reg-fx
 :queue-album-for-upload!
 (fn [_]
   (album-paths {:on-success (fn [paths]
                               (doseq [path paths]
                                 (dispatch [:queue-for-upload path])))
                 :on-failure #(js/console.log "Error:" %)})))

(reg-fx
 :upload-assets!
 (fn [callbacks]
   (upload-assets! callbacks)))
