(ns still.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [still.events]
            [still.subs]
            [still.about :as about]))

(def ReactNative (js/require "react-native"))
(def Camera (js/require "react-native-camera"))

(def network-info (js/require "react-native-network-info"))
(def KeepAwake (js/require "react-native-keep-awake"))
(def keep-awake (.-default KeepAwake))

(def card-stack (r/adapt-react-class (.-CardStack (.-NavigationExperimental ReactNative))))
(def navigation-header-comp (.-Header (.-NavigationExperimental ReactNative)))
(def navigation-header (r/adapt-react-class navigation-header-comp))
(def header-title (r/adapt-react-class (.-Title (.-Header (.-NavigationExperimental ReactNative)))))



(def capture-image (js/require "./images/ic_photo_camera_36pt.png"))

(defn dimensions [from]
  (-> (.-Dimensions ReactNative)
      (.get from)
      (js->clj :keywordize-keys true)))

(def styles
  {:fullscreen {:position "absolute" 
                :top 0 
                :left 0 
                :bottom 0 
                :right 0}
   :container {:flex 1 :padding 40 :align-items "center"
               :background-color "black"}
   :container-no-padding {:flex 1 :background-color "black" :padding 0 :justify-content "center" :align-items "center" :position "relative"}
   :button {:background-color "#999" :padding 10 :border-radius 5 :margin-top 10}
   :preview {:flex 1
             :justify-content "flex-end"
             :align-items "center"}
   :secret {:flex 0}
   :overlay {:position "absolute"
             :padding 16
             :right 0
             :left 0
             :top 1
             :bottom 50
             :align-items "center"}
   :bottom-overlay {:bottom 0
                    :background-color "rgba(0,0,0,0.4)"
                    :flex-direction "row"
                    :justify-content "center"
                    :align-items "center"}
   :capture-button {:padding 15
                    :background-color "white"
                    :border-radius 40}
   :text {:font-family "American Typewriter"}})


(def app-registry (.-AppRegistry ReactNative))
(def navigator (r/adapt-react-class (.-NavigatorIOS ReactNative)))
(def status-bar (r/adapt-react-class (.-StatusBar ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity ReactNative)))
(def camera (r/adapt-react-class (.-default Camera)))
(def scroll-view (r/adapt-react-class (.-ScrollView ReactNative)))

(def logo-img (js/require "./images/cljs.png"))
(def vivian-img (js/require "./images/vivian.png"))

(def secret-camera
  (with-meta
    (fn [opts]
      [camera (update opts :style assoc :width 0 :height 0)])
    {:component-did-mount
     (fn [this]
       (dispatch [:take-delayed-picture]))}))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

#_(defn app-root []
text  (let [greeting (subscribe [:get-greeting])
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

(defn take-picture []
  (fn []
    [view {:style (:container styles)}
     [status-bar {:animated true :hidden true}]
      
     [camera {:type (.. Camera -constants -Type -back)
              :style (:preview styles)}]
     [view {:style (merge (:overlay styles)
                          (:bottom-overlay styles))}
      [touchable-opacity {:style (:capture-button styles)
                          :on-press #(dispatch [:take-picture])}
       [image {:source capture-image}]]]]))

(defn about-view-picture [key]
  [view {:style { :flex 1 }}
   [view {:style (:container-no-padding styles)}
   [status-bar {:animated true :hidden true}]]

   ;[secret-camera {:type (.. Camera -constants -Type -front)
   ;                :style (:secret styles)}]
   [view {:style (:container-no-padding styles)}
       [image {:source (key about/images) :resize-mode "stretch" :style (:fullscreen styles) }]]
   [view {:style (:container-no-padding styles)}
   [text {:style {:margin 10
                   :color "white" :font-family "American Typewriter"}}
     (key about/captions)]]])

(defn about-overview []
  [view {:style (:container styles)}
   [status-bar {:animated true :hidden true}]

   [scroll-view {:style {:background-color "black"}}
    [touchable-opacity {:style (:capture-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-1 :title "About 1"}])}
    [text {:style {:margin 10
                   :color "black" :font-family "American Typewriter"}} "1"     ]]
    [touchable-opacity {:style (:capture-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-2 :title "About 2"}])}
     [text {:style {:margin 10
                    :color "black" :font-family "American Typewriter"}} "2"     ]]
    [touchable-opacity {:style (:capture-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-3 :title "About 3"}])}
     [text {:style {:margin 10
                    :color "black" :font-family "American Typewriter"}} "3"     ]]
    [touchable-opacity {:style (:capture-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-4 :title "About 4"}])}
     [text {:style {:margin 10   :color "black" :font-family "American Typewriter"}} "4"     ]]
    [touchable-opacity {:style (:capture-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-5 :title "About 5"}])}
     [text {:style {:margin 10  :color "black" :font-family "American Typewriter"}} "5"     ]]
    [touchable-opacity {:style (:capture-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-6 :title "About 6"}])}
     [text {:style {:margin 10  :color "black" :font-family "American Typewriter"}} "6"     ]]
    ]])

(defn about []
  [view {:style (:container styles)}
   [status-bar {:animated true :hidden true}]
   
   ;[secret-camera {:type (.. Camera -constants -Type -front)
   ;                :style (:secret styles)}]
    [scroll-view {:style {:background-color "black"}}
    [image {:source vivian-img}]
    [text {:style {:margin 10
                   :color "white" :font-family "American Typewriter"}}
     (:one about/captions)]]])

(defn home-screen []
  [view {:style (:container styles)}
   [status-bar {:animated true :hidden true}]
   
   [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center" :color "white" :font-family "American Typewriter"}} "Still"]
   [image {:source vivian-img}]
   [touchable-highlight {:style (:button styles)
                         :on-press #(dispatch [:nav/push {:key :about
                                                          :title "Info view"}])}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"  :font-family "American Typewriter"}}
     "About Vivian Maier"]]
   [touchable-highlight {:style (:button styles)
                         :on-press #(dispatch [:nav/push {:key :take-picture
                                                          :title "Take picture"  :font-family "American Typewriter"}])}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"  :font-family "American Typewriter"}}
     "Take a picture"]]])

(defn nav-title [props]
  (.log js/console "props" props)
  [header-title (aget props "scene" "route" "title")])

(defn header
  [props]
  [navigation-header
   (assoc
     (js->clj props)
     :render-title-component #(r/as-element (nav-title %))
     :on-navigate-back #(dispatch [:nav/pop nil]))])

(defn scene [props]
  (let [opts (js->clj props :keywordize-keys true)]
    [view {:margin 10} [text (str (-> opts :scene :route :key))]]
  
    (case (-> opts :scene :route :key)
      "first-route" [home-screen]
      "take-picture" [take-picture]
      "about" [about-overview]
      "about-view-1" [about-view-picture :one]
      "about-view-2" [about-view-picture :two]
      "about-view-3" [about-view-picture :three]
      "about-view-4" [about-view-picture :four]
      "about-view-5" [about-view-picture :five]
      "about-view-6" [about-view-picture :six]

      )))

(defn app-root []
  (let [nav (subscribe [:nav/state])]
    (fn []
      [card-stack {:on-navigate-back #(dispatch [:nav/pop nil])
                   :render-overlay   #(r/as-element (header %))
                   :navigation-state @nav
                   :style            {:flex 1}
                   :render-scene     #(r/as-element (scene %))}])))

(defn init []
  (.getSSID network-info #(alert %))
  (alert ( str "keep awake: " (.-isActive keep-awake)))
  ((.-activate keep-awake))
  (alert ( str "keep awake: " (.-isActive keep-awake)))
  (dispatch-sync [:initialize-db {:key :first-route
                                  :title "First Route"}])
  (.registerComponent app-registry "Still" #(r/reactify-component app-root)))
