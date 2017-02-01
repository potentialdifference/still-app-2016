(ns still.ios.events
  (:require [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
            [still.shared :refer [album-paths upload-assets!]]
            [still.events :refer [validate-spec-mw]]
            [still.db :as db :refer [app-db]]
            [clojure.string :as str]))

(def Camera (js/require "react-native-camera"))

(defn request-camera! [callback]
  (.. Camera -CameraManager checkDeviceAuthorizationStatus
      (then callback)
      (catch (fn [rejection]
               ))))

(def DeviceInfo (js/require "react-native-device-info"))
(defn device-name []
  (.getDeviceName DeviceInfo))

(def PushNotification (js/require "react-native-push-notification"))
(.configure PushNotification (clj->js {:onNotification #(.log js/console "notification: " %)}))

(defn push-notify [message]
  (.localNotification PushNotification (clj->js {:subText "Message from 'H'" :message message :autoCancel false})) )


(reg-event-db
  :initialize-db
  validate-spec-mw
  (fn [empty-db [_ route]]
    (let [device-name (device-name)]
      (app-db route device-name))))

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
       :request-camera! #(dispatch [:set-camera-authorized %])
       :store-privacy-agreed! bool})))
(reg-event-fx
  :display-text
  validate-spec-mw
  (fn [{:keys [db]} [_ content]]
    (let [device-name (:device-name db)
          users-name (if (str/includes? device-name "�")
                       (str/replace device-name #"�.+$" "")
                       (if (str/includes? device-name "'")
                         (str/replace device-name #"'.+$" "")
                          device-name))
          content (str/replace content "{name}" users-name)]
      {:db (assoc db :show {:message-content content} :awaiting-show? false)
       :buzz true
       :notify {:message content} })))

(reg-fx
  :notify
  (fn [world [_ _]]
    (let [message (:message world)]
      (js/console.log "notify: " )
      (push-notify message))))