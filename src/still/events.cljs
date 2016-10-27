(ns still.events
  (:require
    [re-frame.core :refer [reg-event-db after dispatch reg-event-fx]]
    [clojure.spec :as s]
    [still.db :as db :refer [app-db]]))

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

(def Camera (js/require "react-native-camera"))

(defn capture []
  (.. Camera -CameraManager
      (capture (clj->js {:target (.. Camera -constants -CaptureTarget -cameraRoll)}))
      (then (fn [data]
              (js/console.log data)))))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
  :initialize-db
  validate-spec-mw
  (fn [empty-db [_ route]]
    (app-db route)))

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

(reg-event-db
  :set-greeting
  validate-spec-mw
  (fn [db [_ value]]
    (assoc db :greeting value)))

(reg-event-db
 :take-picture
 validate-spec-mw
 (fn [{:keys [camera-type] :as db} [_]]
   ;; take picture
   (capture)
   db))

(reg-event-fx
 :take-delayed-picture
 (fn [cofx _]
   {:dispatch-later [{:ms 1000 :dispatch [:take-picture]}]}))
