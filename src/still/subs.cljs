(ns still.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :nav/index
 (fn [db _]
   (get-in db [:nav :index])))

(reg-sub
 :ssid
 (fn [db _]
   (:ssid db)))

(reg-sub
 :nav/state
 (fn [db _]
   (:nav db)))

(reg-sub
 :privacy-policy-agreed?
 (fn [db _]
   (:privacy-policy-agreed? db)))
