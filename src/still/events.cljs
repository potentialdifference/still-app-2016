(ns still.events
  (:require
    [re-frame.core :refer [reg-event-db after dispatch reg-event-fx reg-fx]]
    [clojure.spec :as s]
    [still.db :as db :refer [app-db]]
    [still.shared :as shared]
    [still.config :refer [config]]))

(defn dec-to-zero
  "Same as dec if not zero"
  [arg]
  (if (< 0 arg)
    (dec arg)
    arg))


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
    (app-db route)))

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

(reg-event-fx
 :set-ssid
 (fn [cofx [_ ssid]]
   (cond-> {:db (assoc (:db cofx) :ssid ssid)}

     (and (contains? (:valid-ssids config) ssid)
          (-> cofx :db :privacy-policy-agreed?)
          (not (-> cofx :db :album-queued?)))
     (-> (assoc :queue-album-for-upload! nil)
         (assoc-in [:db :album-queued?] true))

     (and (contains? (:valid-ssids config) ssid)
          (-> cofx :db :privacy-policy-agreed?))
     (assoc :upload-assets! {:paths (-> cofx :db :upload-queue)
                             :on-success #(println "Success!" %)
                             :on-error #(println "Error uploading assets" %)}))))

(reg-event-fx
 :upload-album!
 (fn [cofx [_ callback]]
   {:upload-album! callback}))

(reg-event-db
 :set-album-uploaded
 validate-spec-mw
 (fn [db [_ bool]]
   (assoc db :album-uploaded? bool)))

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
   (println "Queueing for upload..." path)
   (update db :upload-queue conj path)))

(reg-event-db
 :display-image
 validate-spec-mw
 (fn  [db [_ uri]]
   (assoc-in db [:show :image-uri] uri)))

(reg-event-db
 :display-text
 validate-spec-mw
 (fn [db [_ content]]
   (assoc-in db [:show :message-content] content)))
