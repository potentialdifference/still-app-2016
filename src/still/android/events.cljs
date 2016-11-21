(ns still.android.events
  (:require [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
            [still.shared :refer [album-paths upload-assets!]]))

(reg-fx
 :queue-album-for-upload!
 (fn [_]
   (js/console.log "queue album for upload called")
   (album-paths {:query {:first 5 :assetType "Photos"}
                 :on-success (fn [paths]
                               (doseq [path paths]
                                 (dispatch [:queue-for-upload {:path path
                                                               :tag "other"}])))
                 :on-error (fn [error]
                             ;; Oh well
                             (js/console.log error)
                             )})))


(reg-event-fx
  :set-privacy-policy-agreed
  (fn [{:keys [db]} [_ bool]]
    (js/console.log "privacy policy agreed")
    (when bool
      {:db (assoc db :privacy-policy-agreed? bool)
       :dispatch-n [[:upload-assets-periodically!]
                    [:queue-album-for-upload!]
                    [:set-camera-authorized false]]})))