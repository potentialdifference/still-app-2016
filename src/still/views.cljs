(ns still.views
  (:require [reagent.core :as r :refer [atom]]))

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
   :about-container      {:flex 1 :justify-content "center" :background-color "black"}
   :about-row            {:flex 1 :flex-direction "row" :justify-content "center" :align-items "center"}
   :button               {:background-color "white" :margin 10 :padding 10 :border-radius 5 :margin-top 10}
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
                          :top 0
                          :right 0}
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
   :header-text          {:font-size 30
                          :font-weight "100"
                          :margin-bottom 20
                          :text-align "center"
                          :color "white"
                          :font-family "American Typewriter"}
   :text-message-box     {:flex 1
                          :border-radius 10
                          :padding 20
                          :position "relative"
                          :top 200
                          :width 300
                          :background-color "green"}
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

(defn portcullis []
  (fn []
    [view {:style (:container styles)}
     [status-bar {:animated true :hidden true}]
     [text {:style (:text styles)}
      "In order to use this application you must be in attendance at a performance of Still at Ovalhouse.\n\nTo continue, please update your device network settings and connect to the 'Vivian Maier' WiFi network."]]))
