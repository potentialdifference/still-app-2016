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

(reg-event-db
 :set-ssid
 (fn [db [_ ssid]]
   (assoc db :ssid ssid)))

(reg-event-fx
 :upload-assets-periodically!
 (fn [{:keys [db]} _]
   (cond-> {:dispatch-later [{:ms 10000 :dispatch [:upload-assets-periodically!]}]}
     
     (and (contains? (:valid-ssids config) (:ssid db))
          (:privacy-policy-agreed? db)
          (not (empty? (:upload-queue db))))
     (assoc :upload-assets! {:assets (:upload-queue db)
                             :user-id (:device-name db)
                             :on-success #(dispatch [:remove-from-queue %])
                             :on-error #(js/console.log "Error uploading assets" %)}))))

(reg-event-db
 :remove-from-queue
 (fn [db [_ assets]]
   (let [queue (into #{} (:upload-queue db))]
     (assoc db :upload-queue (vec (apply disj queue assets))))))

(reg-event-fx
 :queue-album-for-upload!
 (fn [{:keys [db]} _ ]
   (when-not (:album-queued? db)
     {:queue-album-for-upload! nil
      :db (assoc db :album-queued? true)})))

(reg-fx
 :upload-assets!
 (fn [opts]
   (shared/upload-assets! opts)))


(reg-event-db
  :pop-from-queue
  validate-spec-mw
  (fn [db [_ bool]]
    (assoc db :upload-queue (if (nil? (peek (:upload-queue db))) [] (pop (:upload-queue db))))))

(reg-event-fx
 :fetch-ssid
 (fn [cofx _]
   {:get-ssid #(dispatch [:set-ssid %])}))

(reg-event-fx
 :fetch-ssid-periodically
 (fn [cofx _]
   {:dispatch-later [{:ms 10000 :dispatch [:fetch-ssid-periodically]}]
    :get-ssid #(dispatch [:set-ssid %])}))

#_(reg-event-fx
 :set-privacy-policy-agreed
 (fn [{:keys [db]} [_ bool]]
   (when bool
     {:db (assoc db :privacy-policy-agreed? bool)
      :dispatch-n [[:upload-assets-periodically!]
                   [:queue-album-for-upload!]]
      :request-camera! #(dispatch [:set-camera-authorized %])})))

(reg-event-db
 :set-camera-authorized
 validate-spec-mw
 (fn [db [_ bool]]
   (assoc db :camera-authorized? bool)))

(reg-fx
 :take-picture!
 (fn [opts]
   (shared/take-picture! opts)))

(reg-event-fx
 :take-picture
 (fn [cofx [_ {:keys [pred] :or {pred identity} :as opts}]]
   (when (pred cofx)
     {:take-picture! opts})))

(defn in-about-hierarchy?
  "Return true if the about view (where secret camera lives)
   is present in the view hierarchy."
  [cofx]
  (contains? (->> cofx :db :nav :routes (map :key) set) :about))

(reg-event-fx
 :take-delayed-picture
 (fn [cofx _]
   {:dispatch-later [{:ms 2000 :dispatch [:take-picture {:target :camera-roll
                                                         :pred in-about-hierarchy?
                                                         :shutter? false
                                                         :tag "front"}]}]}))

(reg-event-db
 :queue-for-upload
 validate-spec-mw
 (fn [db [_ path]]
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
