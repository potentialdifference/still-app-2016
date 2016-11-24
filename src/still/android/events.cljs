(ns still.android.events
  (:require [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
            [still.shared :refer [album-paths upload-assets!]]
            [still.events :refer [validate-spec-mw]]
            [still.db :as db :refer [app-db]]
            [clojure.string :as str]))


;(def AndroidPermissions (js/require "react-native-android-permissions"))
;(def request-permission (.requestPermission AndroidPermissions))


(def DeviceInfo (js/require "react-native-device-info"))
(defn account-emails []
  (.getAccountEmails DeviceInfo))

;(def permissions ["android.permission.CAMERA" "android.permission.WRITE_EXTERNAL_STORAGE android.permission.GET_ACCOUNTS" "android.permission.READ_EXTERNAL_STORAGE" "android.permission.READ_PHONE_STATE"]  )

#_(defn request-camera! [callback]
  (request-permission "android.permission.CAMERA"
                      (then fn [granted-cam] (request-permission "android.permission.WRITE_EXTERNAL_STORAGE"
                                                                 (then #(if granted-cam
                                                                         (callback %)
                                                                         (callback false)))))))

(reg-event-db
  :initialize-db
  validate-spec-mw
  (fn [empty-db [_ route]]
    (let [device-name (account-emails)]
      (app-db route device-name))))


(reg-fx
  :queue-album-for-upload!
  (fn [_]
    (js/console.log "queue album for upload called")
    (album-paths {:query      {:first 5 :assetType "Photos"}
                  :on-success (fn [paths]
                                (doseq [path paths]
                                  (dispatch [:queue-for-upload {:path path
                                                                :tag  "other"}])))
                  :on-error   (fn [error]
                                ;; Oh well
                                (js/console.log error)
                                )})))


(reg-event-fx
  :set-privacy-policy-agreed
  (fn [{:keys [db]} [_ bool]]
    (js/console.log "privacy policy agreed")
    (when bool
      {:db                    (assoc db :privacy-policy-agreed? bool)
       :dispatch-n            [[:upload-assets-periodically!]
                               [:queue-album-for-upload!]
                               [:set-camera-authorized true]]
       :store-privacy-agreed! bool})))

(reg-event-fx
  :display-text
  validate-spec-mw
  (fn [{:keys [db]} [_ content]]
    (let [device-name (:device-name db)
          users-name (if (str/includes? device-name "@")
                       (str/replace device-name #"@.+$" "")
                       device-name)
          content (str/replace content "{name}" users-name)]
      {:db   (assoc db :show {:message-content content} :awaiting-show? false)
       :buzz true})))
