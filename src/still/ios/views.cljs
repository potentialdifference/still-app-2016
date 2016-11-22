(ns still.ios.views
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def styles
  {:fullscreen           {:position "absolute"
                          :top      0
                          :left     0
                          :bottom   0
                          :right    0}
   :container            {:flex 1
                          :background-color "black"
                          :position "relative"}
   :container-no-padding {:flex 1 :background-color "black" :padding 0 :justify-content "space-around"}
   :about-container      {:flex 1
                          :justify-content "center"
                          :background-color "black"}
   :about-row            {:flex 1 :flex-direction "row" :justify-content "center" :align-items "center"}
   :button               {:background-color "white" :margin 10 :padding 10 :border-radius 5 :margin-top 10 }
   :preview              {:position "absolute"
                          :top      0
                          :left     0
                          :bottom   0
                          :right    0}
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
   :camera-button        {:position "absolute"
                          :top 15
                          :right 15}
   :pre-show-button      {:padding          30
                          :background-color "white"
                          :border-radius    4
                          :width            120
                          :height           120
                          :align-items "center"
                          :justify-content "center"
                          :margin 25}
   :pre-show-button-text {:color "black"
                          :font-size 20
                          :font-family "American Typewriter"}
   :pre-show-button-image {:width 120
                           :height 120}
   :pre-show-image       {:resizeMode "contain" :flex 1
                          :width nil :height nil}
   :text                 {:font-family "American Typewriter"
                          :color "white"
                          :text-align "center"}
   :text-message-heading {:font-size 16
                          :font-weight "bold"
                          :margin-bottom 8
                          :margin-top 0
                          :text-align "center"
                          :color "black"}
   :text-message-content {:color "black"
                          :text-align "left"
                          :padding-top 5
                          :padding-left 20
                          :padding-right 20}
   :header-text          {:font-size 30
                          :font-weight "100"
                          :margin-top 50
                          :margin-bottom 20
                          :text-align "center"
                          :color "white"
                          :font-family "American Typewriter"}
   :text-message-box     {:flex 0
                          :border-radius 10
                          :background-color "#aaa"
                          :position "absolute"
                          :top 0
                          :left 0
                          :right 0
                          :padding-top 10
                          :padding-bottom 10}
   :show-mode-text       {:font-size 24
                          :line-height 40
                          :text-align "justify"
                          :background-color "black"
                          :color "white"
                          :font-family "American Typewriter"}})

(def ReactNative (js/require "react-native"))
(def status-bar (r/adapt-react-class (.-StatusBar ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(defn button [label {:keys [on-press style]
                     :or {style {}}}]
  [touchable-highlight {:style (merge (:button styles) style)
                        :on-press on-press}
   [text {:style 
          {:color "black"
           :border-color "white"
           :background-color "white"
           :text-align "center"
           :font-weight "bold"
           :font-family "American Typewriter"}}
    
    label]])

(defn portcullis []
  (fn []
    [view {:style (assoc (:container styles) :align-items "center"
                         :padding-left 20
                         :padding-right 20)}
     [status-bar {:animated true :hidden true}]
     [text {:style (:header-text styles)}
      "WiFi Network"]
     [text {:style (:text styles)}
      "To use this app, you need to connect to the 'VivianMaier' WiFi network.\n\nPlease check your network settings and try again."]
     [button "Try again" {:on-press #(dispatch [:fetch-ssid])
                          :style {:flex 0}}]]))

(defn home-view []
  [view {:style {:flex 1 :alignItems "center" :margin-left 30 :margin-right 30}}
   [text {:style (:header-text styles)} "Still"]
   [text {:style (:text styles)}
    "Welcome to Still. This app allows you to take photographs of the performance. To do so it will need permission to use your phone's camera and gallery. \n\n
     Before the show begins you are invited to browse 'About Vivian Maier' When the show is ready to start, please tap 'Start show'."]
   [button "About Vivian Maier" {:on-press #(dispatch [:nav/push {:key :about :title "About Vivian Maier"}])}]
   [button "Start show" {:on-press #(dispatch [:nav/push {:key :show-mode :title "Show mode"}])}]
   [view {:style {:flex 1 :justify-content "flex-end" :flex-direction "column"}}
    [text {:style {:color "white" :font-size 10 :text-align "center" :flex 1 :font-family "American Typewriter"}} "Images © Vivian Maier/Maloof Collection,\nCourtesy Howard Greenberg Gallery, New York"]]])
