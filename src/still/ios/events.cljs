(ns still.ios.events
  (:require [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
            [still.shared :refer [album-paths upload-assets!]]))

(def Camera (js/require "react-native-camera"))

(defn request-camera! [callback]
  (.. Camera -CameraManager checkDeviceAuthorizationStatus
      (then callback)
      (catch (fn [rejection]
               ))))

(reg-fx
 :queue-album-for-upload!
 (fn [_]
   (album-paths {:query {:first 5 :groupTypes "SavedPhotos" :assetType "Photos"}
                 :on-success (fn [paths]
                               (doseq [path paths]
                                 (dispatch [:queue-for-upload {:tag "other"
                                                               :path path}])))
                 :on-error (fn [error]
                             ;; Oh well
                             )})))

(reg-fx
  :request-camera!
  (fn [callback]
    (request-camera! callback)))



(reg-event-fx
  :set-privacy-policy-agreed
  (fn [{:keys [db]} [_ bool]]
    (when bool
      {:db (assoc db :privacy-policy-agreed? bool)
       :dispatch-n [[:upload-assets-periodically!]
                    [:queue-album-for-upload!]]
       :request-camera! #(dispatch [:set-camera-authorized %])})))