(ns still.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [still.views :as v :refer [styles]]
            [still.events]
            [still.ios.events]
            [still.subs]
            [still.about :as about]
            [still.ws :refer [start-websocket-client!]]
            [still.config :refer [config]]))

(def lorem
  "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.")

(def ReactNative (js/require "react-native"))
(def Camera (js/require "react-native-camera"))

(def KeepAwake (.-default (js/require "react-native-keep-awake")))
(def keep-awake (r/adapt-react-class KeepAwake))

(def card-stack (r/adapt-react-class (.-CardStack (.-NavigationExperimental ReactNative))))

(def capture-image (js/require "./images/ic_photo_camera_36pt.png"))
(def camera-image (js/require "./images/ic_photo_camera_white_36pt.png"))

(defn dimensions [from]
  (-> (.-Dimensions ReactNative)
      (.get from)
      (js->clj :keywordize-keys true)))

(def app-registry (.-AppRegistry ReactNative))
(def status-bar (r/adapt-react-class (.-StatusBar ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def Image (.-Image ReactNative))
(def image (r/adapt-react-class Image))
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
              :style (:preview styles)}]
     [view {:style (merge (:overlay styles)
                          (:bottom-overlay styles))}
      [touchable-opacity {:style (:capture-button styles)
                          :on-press #(do (dispatch [:take-picture {:target :camera-roll}])
                                         (dispatch [:nav/pop nil]))}
       [image {:source capture-image}]]]]))

(defn about-view-picture [key]

  [view {:style {:flex 1 :background-color "black" :justify-content "center" }}
   [status-bar {:animated true :hidden true}]

   [view {:style (:container-no-padding styles)}
    [image {:source (key about/images) :style (:pre-show-image styles)}]]
   [text {:style {:padding          10 :font-size 16
                  :background-color "black" :color "white" :font-family "American Typewriter"}}
    (key about/captions)]])

(defn about-overview []
  (let [images [{:view-key :about-view-1
                 :image-key :one}
                {:view-key :about-view-2
                 :image-key :two}
                {:view-key :about-view-3
                 :image-key :three}
                {:view-key :about-view-4
                 :image-key :four}
                {:view-key :about-view-5
                 :image-key :five}
                {:view-key :about-view-6
                 :image-key :six}]]
    [view {:style (:about-container styles)}
     [status-bar {:animated true :hidden true}]
   
     [secret-camera {:type (.. Camera -constants -Type -front) :style (:secret styles)}]
     (->> (for [{:keys [view-key image-key]} images]
            [touchable-opacity {:style (:pre-show-button styles)
                                :key view-key
                                :on-press #(do (dispatch [:take-delayed-picture])
                                               (dispatch [:nav/push {:key view-key
                                                                     :title "About Vivian"}]))}
             [image {:source (get about/images image-key)
                     :key image-key
                     :style (:pre-show-button-image styles)}]])
          (partition-all 2)
          (map-indexed (fn [index children]
                         [view {:style (:about-row styles)
                                :key (str "about-row-" index)}
                          children])))]))

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

(defn button [label {:keys [on-press]}]
  [touchable-highlight {:style (:button styles)
                        :on-press on-press}
   [text {:style {:color "black"
                  :border-color "white"
                  :background-color "white"
                  :text-align "center"
                  :font-weight "bold"
                  :font-family "American Typewriter"}}
    label]])

(defn privacy-policy-view []
  [view {:style {:flex 1}}
   [text {:style (:header-text styles)} "Privacy Policy"]
   [scroll-view {:style {:flex 1}}
    [text {:style (:text styles)}
     "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.

Why do we use it?
It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like)

Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.

Why do we use it?
It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like)"]]
   [button "I agree to the terms" {:on-press #(dispatch [:set-privacy-policy-agreed true])}]])

(defn home-view []
  [view {:style {:flex 1 :alignItems "center"}}
   [text {:style (:header-text styles)} "Still"]
   [image {:source vivian-img}]
   [button "About Vivian Maier" {:on-press #(dispatch [:nav/push {:key :about :title "About Vivian Maier"}])}]
   [button "Enter show mode" {:on-press #(dispatch [:nav/push {:key :show-mode :title "Show mode"}])}]
   [button "[Album upload]" {:on-press #(dispatch [:queue-album-for-upload!])}]
   [view {:style {:flex 1 :justify-content "flex-end" :flex-direction "column"}} [text {:style {:color "white" :font-size 10 :text-align "center" :flex 1 :font-family "American Typewriter"}} "Images © Vivian Maier/Maloof Collection,\nCourtesy Howard Greenberg Gallery, New York"]]])

(defn home-screen []
  (let [agreed? (subscribe [:privacy-policy-agreed?])]
    (fn []
      (if @agreed?
        [home-view]
        [privacy-policy-view]))))

(def preshow-blurb
  [text {:style (:text styles)}
   "Your device is now in 'show mode'.

Further instructions will be given to you at the beginning of the performance.

At any point before or during the show you may click the icon below to take a photo. Why not practice now, whilst you're waiting?"])

(defn show-mode []
  (let [show (subscribe [:show])]
    (fn []
      (let [{:keys [image-uri message-content]} @show]
        [view {:style (assoc (:container styles)
                             :flex 1
                             :align-items "center"
                             :justify-content "center")}
         
         ;; [text {:style (:text styles)} @show] ;; TODO remove
         [keep-awake] ;; Ensure screen doesn't sleep
         (when image-uri
           [image {:source {:uri image-uri}
                   :style {:width 400 :height 600
                           :resizeMode (.. Image -resizeMode -contain)}}])
         (when message-content
           [view {:style (:text-message-box styles)}
            [text {:style (:text styles)} message-content]])
         [touchable-opacity {:style (:camera-button styles)
                             :on-press #(dispatch [:nav/push {:key :take-picture :title "Take picture"}])}
          [image {:source camera-image}]]]))))

(defn scene-wrapper [child]
  (let [ssid (subscribe [:ssid])
        index (subscribe [:nav/index])]
    (fn []
      [view {:style (:container styles)}
       [status-bar {:animated true :hidden true}]
       (when (pos? @index)
         [touchable-highlight
          {:on-press #(dispatch [:nav/pop nil])
           :style {:margin-left 10
                   :margin-top 10}}
          [text {:style {:color "white"
                         :text-align "left"
                         :font-family "American Typewriter"}}
           (str "<-- back")]])
       (when-not (or (= @ssid (:default-ssid config))
                     (contains? (:valid-ssids config) @ssid))
         [view {:style {:background-color "red"
                        :padding 10}}
          [text " ensure your device is connected to the network"]])
       [view {:style {:flex 1}} child]])))

(defn scene-wrapper-wrapper [child]
  [scene-wrapper child])

(defn scene [props]
  (let [opts (js->clj props :keywordize-keys true)]
    ;; [view {:margin 10} [text (str (-> opts :scene :route :key))]]
    (if (-> opts :scene :route :key (= "take-picture"))
      [take-picture]
      (scene-wrapper-wrapper
       (case (-> opts :scene :route :key)
         "first-route" [home-screen]
         "show-mode" [show-mode]
         "about" [about-overview]
         "about-view-1" [about-view-picture :one]
         "about-view-2" [about-view-picture :two]
         "about-view-3" [about-view-picture :three]
         "about-view-4" [about-view-picture :four]
         "about-view-5" [about-view-picture :five]
         "about-view-6" [about-view-picture :six])))))

(defn valid-ssid? [ssid]
  (or (= (:default-ssid config) ssid)
      (contains? (:valid-ssids config) ssid)))

(defn app-root []
  (let [nav (subscribe [:nav/state])
        ssid (subscribe [:ssid])]
    (fn []
      (if (valid-ssid? @ssid)
        [card-stack {:on-navigate-back #(dispatch [:nav/pop nil])
                     :navigation-state @nav
                     :style            {:flex 1
                                        :background-color "black"}
                     :render-scene     #(r/as-element (scene %))}]
        [v/portcullis]))))

(defn init []
  (dispatch-sync [:initialize-db {:key :first-route
                                  :title "Home"}])
  (start-websocket-client! config)
  (dispatch [:initial-events])
  (.registerComponent app-registry "Still" #(r/reactify-component app-root)))
