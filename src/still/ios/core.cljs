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
  {:fullscreen           {:position "absolute"
                          :top      0
                          :left     0
                          :bottom   0
                          :right    0}
   :container            {:flex 1 :padding 40 :align-items "center" :background-color "black" :position "relative"}
   :container-no-padding {:flex 1 :background-color "black" :padding 0 :justify-content "space-around"}
   :about-container      {:flex 1 :justify-content "center" :background-color "black"}
   :about-row            {:flex 1 :flex-direction "row" :justify-content "center" :align-items "center"}
   :button               {:background-color "white" :margin 10 :padding 10 :border-radius 5 :margin-top 10}
   :preview              {:position "absolute"
                          :top      0
                          :left     0
                          :bottom   0
                          :right    0
                          ;:flex 1
                          ;:justify-content "flex-end"
                          ;:align-items "center"
                          }
   :secret               {:flex 0}
   :overlay              {:position    "absolute"
                          :padding     16
                          :right       0
                          :left        0
                          ;:top         1
                          :bottom      30
                          :align-items "center"
                          }
   :bottom-overlay       {:bottom           0
                          :background-color "rgba(0,0,0,0.4)"
                          :flex-direction   "row"
                          :justify-content  "center"
                          :align-items      "center"
                          }
   :capture-button       {:padding          15
                          :background-color "white"
                          :border-radius    40}
   :pre-show-button      {:padding          30
                          :background-color "white"
                          :border-radius    4
                          :width            120
                          :height           120 :align-items "center" :justify-content "center" :margin 50}
   :pre-show-button-text {
                          :color "black" :font-size 20 :font-family "American Typewriter"}
   :pre-show-image       {:resizeMode "contain" :flex 1 :width nil :height nil}
   :text                 {:font-family "American Typewriter"}})


(def app-registry (.-AppRegistry ReactNative))
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


(defn take-picture []
  (fn []
    [view {:style (:container styles)}
     [status-bar {:animated true :hidden true}]

     [camera {:type (.. Camera -constants -Type -back)
              :style (:preview styles)}      ]
     [view {:style (merge (:overlay styles)
                          (:bottom-overlay styles))}
      [touchable-opacity {:style (:capture-button styles)
                          :on-press #(dispatch [:take-picture])}
       [image {:source capture-image}]]]]))

(defn about-view-picture [key]

  [view {:style {:flex 1 :background-color "black" :justify-content "center" }}

   [status-bar {:animated true :hidden true}]


   ;[secret-camera {:type (.. Camera -constants -Type -front)
   ;                :style (:secret styles)}]
   [view {:style (:container-no-padding styles)}
    [image {:source (key about/images) :style (:pre-show-image styles)}]]
   [text {:style {:padding          10 :font-size 16
                  :background-color "black" :color "white" :font-family "American Typewriter"}}
    (key about/captions)  ]


   ])


(defn about-overview []

  [view {:style (:about-container styles)}

   [status-bar {:animated true :hidden true}]


   [view {:style (:about-row styles)}
    [touchable-opacity {:style (:pre-show-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-1 :title "About Vivian 1"}])}
     [text {:style (:pre-show-button-text styles) } "1"     ]]
    [touchable-opacity {:style (:pre-show-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-2 :title "About Vivian 2"}])}
     [text {:style (:pre-show-button-text styles)} "2"     ]]]

   [view {:style (:about-row styles)}
    [touchable-opacity {:style (:pre-show-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-3 :title "About Vivian 3"}])}
     [text {:style (:pre-show-button-text styles)} "3"     ]]
    [touchable-opacity {:style (:pre-show-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-4 :title "About Vivian 4"}])}
     [text {:style (:pre-show-button-text styles)} "4"     ]]]

   [view {:style (:about-row styles)}
    [touchable-opacity {:style (:pre-show-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-5 :title "About Vivian 5"}])}
     [text {:style (:pre-show-button-text styles)} "5"     ]]
    [touchable-opacity {:style (:pre-show-button styles)
                        :on-press #(dispatch [:nav/push {:key :about-view-6 :title "About Vivian 6"}])}
     [text {:style (:pre-show-button-text styles)} "6"     ]]]
   ])

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
                                                          :title "About Vivian Maier"}])}
    [text {:style {:color "black" :border-color "white" :background-color "white" :text-align "center" :font-weight "bold"  :font-family "American Typewriter"}}
     "About Vivian Maier"]]
   [touchable-highlight {:style (:button styles)
                         :on-press #(dispatch [:nav/push {:key :take-picture
                                                          :title "Take picture"  :font-family "American Typewriter"}])}
    [text {:style {:color "black" :border-color "white" :background-color "white" :text-align "center" :font-weight "bold"  :font-family "American Typewriter"}}
     "Take a picture"]]
   [view {:style {:flex 1 :justify-content "flex-end" :flex-direction "column"}} [text {:style {:color "white" :font-size 10 :text-align "center" :flex 1 :font-family "American Typewriter"}}
                                                                                  "Images ©Vivian Maier/Maloof Collection, Courtesy Howard Greenberg Gallery, New York"]] ])

(defn nav-title [props]
  (.log js/console "props" props)
  [header-title {:style {:color "white"}} (aget props "scene" "route" "title")])

(defn header
  [props]
  [navigation-header
   (assoc
     (js->clj props)
     :render-title-component #(r/as-element (nav-title %))
     :on-navigate-back #(dispatch [:nav/pop nil])
     :style {:background-color "white" :height 40 :margin 0 :border-bottom-color "white"
             }
     )])

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
                   :render-header   #(r/as-element (header %))
                   :navigation-state @nav
                   :style            {:flex 1}
                   :render-scene     #(r/as-element (scene %))}])))

(defn init []
  (dispatch-sync [:initialize-db {:key :first-route
                                  :title "Home"}])
  (.registerComponent app-registry "Still" #(r/reactify-component app-root)))
