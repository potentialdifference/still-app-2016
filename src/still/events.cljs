(ns still.events
  (:require
    [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
    [clojure.spec :as s]
    [clojure.string :as str]
    [still.db :as db :refer [app-db]]
    [still.shared :as shared]
    [still.config :refer [config]]))

(def ReactNative (js/require "react-native"))
(def Vibration (.-Vibration ReactNative))

(defn dec-to-zero
  "Same as dec if not zero"
  [arg]
  (if (< 0 arg)
    (dec arg)
    arg))

(def DeviceInfo (js/require "react-native-device-info"))
(defn device-name []
  (.getDeviceName DeviceInfo))


;; -- Middleware ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;

(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check failed: " explain-data) explain-data)))))

(def validate-spec-mw
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
  :initialize-db
  validate-spec-mw
  (fn [empty-db [_ route]]
    (let [device-name (device-name)]
      (app-db route device-name))))

(reg-event-db
 :initial-events
 validate-spec-mw
 (fn [db _]
   (js/console.log "fetch ssid")
   (dispatch [:fetch-ssid-periodically])
   db))

(reg-event-db
  :nav/push
  validate-spec-mw
  (fn [db [_ value]]
    (-> db
        (update-in [:nav :index] inc)
        (update-in [:nav :routes] #(conj % value)))))

(reg-event-db
  :nav/pop
  validate-spec-mw
  (fn [db [_ _]]
    (-> db
        (update-in [:nav :index] dec-to-zero)
        (update-in [:nav :routes] pop))))

(reg-event-db
  :nav/home
  validate-spec-mw
  (fn [db [_ _]]
    (-> db
        (assoc-in [:nav :index] 0)
        (assoc-in [:nav :routes] (vector (get-in db [:nav :routes 0]))))))

(reg-fx
 :get-ssid
 (fn [callback]
   (shared/fetch-ssid callback)))

(reg-event-fx
 :set-ssid
 (fn [cofx [_ ssid]]
   (js/console.log (str "upload queue" (-> cofx :db :upload-queue)))
   (cond-> {:db (assoc (:db cofx) :ssid ssid)}

     (and (contains? (:valid-ssids config) ssid)
          (-> cofx :db :privacy-policy-agreed?)
          (not (-> cofx :db :album-queued?)))
     (-> (assoc :queue-album-for-upload! nil)
         (assoc-in [:db :album-queued?] true))

     (and (contains? (:valid-ssids config) ssid)
          (-> cofx :db :privacy-policy-agreed?)
          (not (nil? (-> cofx :db :upload-queue peek))))
     (assoc :upload-assets! {:paths (-> cofx :db :upload-queue)
                             ;TODO! this is dangerous - quickfix but please replace me!
                             :on-success (fn [response] (dispatch [:pop-from-queue])
                                           (js/console.log "Success! Set to true" response))
                             :on-error #(js/console.log "Error uploading assets" %)
                             :device-name (-> cofx :db :device-name)}))))

(reg-event-fx
 :upload-album!
 (fn [cofx [_ callback]]
   {:upload-album! callback}))


(reg-event-db
  :pop-from-queue
  validate-spec-mw
  (fn [db [_ bool]]
    (assoc db :upload-queue (if (nil? (peek (:upload-queue db))) [] (pop (:upload-queue db))))))

(reg-event-fx
 :fetch-ssid-periodically
 (fn [cofx _]
   {:dispatch-later [{:ms 10000 :dispatch [:fetch-ssid-periodically]}]
    :get-ssid #(dispatch [:set-ssid %])}))

(reg-event-db
 :set-privacy-policy-agreed
 validate-spec-mw
 (fn [db [_ bool]]
   (assoc db :privacy-policy-agreed? bool)))

(reg-fx
 :take-picture!
 (fn [_]
   (shared/take-picture!)))

(reg-event-fx
 :take-picture
 (fn [cofx _]
   {:take-picture! nil}))

(reg-event-db
 :queue-for-upload
 validate-spec-mw
 (fn [db [_ path]]
   (js/console.log "Queueing for upload..." path)
   (update db :upload-queue conj path)))

(reg-fx
 :buzz
 (fn [world [_ millis]]
   (.vibrate Vibration)))

(reg-event-fx
 :display-image
 validate-spec-mw
 (fn  [{:keys [db]} [_ uri]]
   {:db (assoc db :show {:image-uri uri})
    :buzz true}))

(reg-event-fx
  :hide-image
  validate-spec-mw
  (fn  [{:keys [db]} [_ _]]
    {:db (assoc db :show {})
     :buzz true}))

(reg-event-fx
 :display-text
 validate-spec-mw
 (fn [{:keys [db]} [_ content]]
   (let [device-name (:device-name db)
         users-name (if (str/includes? device-name "’")
                        (str/replace device-name #"’.+$" "")
                        ;TODO: Do we also need to match on ' as well as ’ ?
                        device-name)
         content (str/replace content "{name}" users-name)]
     {:db (assoc db :show {:message-content content})
      :buzz true})))
