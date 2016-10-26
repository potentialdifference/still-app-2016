(ns still.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [still.events]
            [still.subs]))

(def ReactNative (js/require "react-native"))
(def Camera (js/require "react-native-camera"))

(def capture-image (js/require "./images/ic_photo_camera_36pt.png"))

(defn dimensions [from]
  (-> (.-Dimensions ReactNative)
      (.get from)
      (js->clj :keywordize-keys true)))

(def styles
  {:container {:flex 1}
   :preview {:flex 1
             :justify-content "flex-end"
             :align-items "center"}
   :overlay {:position "absolute"
             :padding 16
             :right 0
             :left 0
             :align-items "center"}
   :bottom-overlay {:bottom 0
                    :background-color "rgba(0,0,0,0.4)"
                    :flex-direction "row"
                    :justify-content "center"
                    :align-items "center"}
   :capture-button {:padding 15
                    :background-color "white"
                    :border-radius 40}})

(def app-registry (.-AppRegistry ReactNative))
(def navigator (.-Navigator ReactNative))
(def status-bar (r/adapt-react-class (.-StatusBar ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity ReactNative)))
(def camera (r/adapt-react-class (.-default Camera)))

(def logo-img (js/require "./images/cljs.png"))

(def secret-camera
  (with-meta
    (fn [opts]
      [camera (update opts :style assoc :width 0 :height 0)])
    {:component-did-mount
     (fn [this]
       (dispatch [:take-delayed-picture]))}))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])
        {:keys [width height]} (dimensions "window")
        camera-type (subscribe [:camera-type])]
    (fn []
      [view {:style (:container styles)}
       [status-bar {:animated true :hidden true}]
      
       [secret-camera {:type (.. Camera -constants -Type -front)
                       :style (assoc (:preview styles) :width 1 :height 1)}]
       [view {:style (merge (:overlay styles)
                            (:bottom-overlay styles))}
        [touchable-opacity {:style (:capture-button styles)
                            :on-press #(dispatch [:take-picture])}
         [image {:source capture-image}]]]])))

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "Still" #(r/reactify-component app-root)))
