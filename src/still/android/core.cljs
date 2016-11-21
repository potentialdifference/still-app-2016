(ns still.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [still.views :as v :refer [styles button]]
            [still.events]
            [still.android.events]
            [still.subs]
            [still.about :as about]
            [still.ws :refer [start-websocket-client!]]
            [still.config :refer [config]]))

(enable-console-print!)

(def lorem
  "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.")

(def ReactNative (js/require "react-native"))
(def Camera (js/require "react-native-camera"))


(def KeepAwake (.-default (js/require "react-native-keep-awake")))
(def keep-awake (r/adapt-react-class KeepAwake))

(def card-stack (r/adapt-react-class (.-CardStack (.-NavigationExperimental ReactNative))))

(def capture-image (js/require "./images/ic_photo_camera_36pt.png"))
(def camera-image (js/require "./images/ic_photo_camera_white_36pt.png"))
(def message-icon (js/require "./images/message.png"))


(defn dimensions [from]
  (-> (.-Dimensions ReactNative)
      (.get from)
      (js->clj :keywordize-keys true)))


(def app-registry (.-AppRegistry ReactNative))
(def status-bar (r/adapt-react-class (.-StatusBar ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def Image (.-Image ReactNative))
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
      [camera {:type (.. Camera -constants -Type -front)
                :style (:preview styles)
               ;:style (:secret-android styles)
               }

       ])
    {:component-did-mount
     (fn [this]
       ;   (dispatch [:take-delayed-picture {:target :disk}])
       )}))

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
      [touchable-opacity {:style    (:capture-button styles)
                          :on-press #(do (dispatch [:take-picture {:target   :disk
                                                                   :shutter? true
                                                                   :type     :rear}])
                                         ;(dispatch [:nav/pop nil])
                                         )}
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
  (let [camera-authorized? (subscribe [:camera-authorized?])
        images [{:view-key :about-view-1
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
    (fn []
      (js/console.log "camera authorised? " @camera-authorized?)
      [view {:style (:about-container styles)}
       [status-bar {:animated true :hidden true}]

       (when @camera-authorized?
         #_[secret-camera                                     ;{:type (.. Camera -constants -Type -front) :style (:secret styles)}
          ]                                                 ;removing secret camera on Android for now
         )
       (->> (for [{:keys [view-key image-key]} images]
              [touchable-opacity {:style (:pre-show-button styles)
                                  :key view-key
                                  :on-press #(do #_(when @camera-authorized?
                                                   ( dispatch [:take-delayed-picture {:target :disk}]))
                                              ;removing secret camera on Android for now
                                                 (dispatch [:nav/push {:key view-key
                                                                       :title "About Vivian"}]))}
               [image {:source (get about/images image-key)
                       :key image-key
                       :style (:pre-show-button-image styles)}]])
            (partition-all 2)
            (map-indexed (fn [index children]
                           [view {:style (:about-row styles)
                                  :key (str "about-row-" index)}
                            children])))])))


(defn about []
  [view {:style (:container styles)}
   [status-bar {:animated true :hidden true}]

   ;[secret-camera {:type (.. Camera -constants -Type -front)
   ;                :style (:secret styles)}]
   [scroll-view {:style {:background-color "black"}}
    [text {:style {:margin 10
                   :color "white" :font-family "American Typewriter"}}
     (:one about/captions)]]])


(defn privacy-policy-view []
  [view {:style {:flex 1}}
   [text {:style (:header-text styles)} "Privacy Policy"]
   [scroll-view {:style {:flex 1}}
    [text {:style (:text styles)}
     "The Future is Unwritten Privacy Policy for the Still App\nThe Future is Unwritten (TFIU) is a theatre company powered by punk values of honesty, integrity, inclusion and the spirit of Do It Yourself invention; exploring issues of popular culture, politics, spirituality, responsibility, injustice and respect. \n\nTFIU was founded in 2009 by Brighton based writer and director Paul Hodson and the company is run with independent producer Emily Coleman.\n\nTFIU Privacy Policy\nThis privacy policy outlines how the Still App will work and how TFIU will use, transfer and destroy any data taken from the user. By agreeing to this privacy policy and downloading the app you agree for TFIU to use data provided and harvested from your device. \n\nTFIU have no intention to, and will not, save your data after the performance you are attending is over and your data will be destroyed immediately afterwards.\n\nThe Still App\nThe Still App has been made by a third party software developer contracted by TFIU. This company has created the app based on the experience we want audiences to have during a live performance of Still. \n\nThe Still App is designed to only be used alongside the live performances of Still.\n\nIf you are using the Still App during one of these performances, you will have been invited to download the Still App and connect to the Still Wifi network. The app will be used to display information about the photographs of Vivian Maier for users to interact with during the performances. It will also allow audience members to take photographs during the performances. And it will collect data from your phone or device.\n\n\nWhat data is collected by Still App?\n\nIn the case of the Still app, we refer to data as photographs and email address\nIf you are using Still App during one of performances and you are connected to the Still Wifi Network, the following data will be collected as part of using the app:\n\n•\tThe email address associated with the primary account on your device\n•\tAny photographs you take out of the rear facing camera using the “take photograph” feature of the app\n•\tPhotographs which will be silently taken out of the front facing camera (where available) whenever you use the “take photograph” feature of the app\n•\tFive other recent photographs taken from your phone’s gallery. These will be the five most recent photos taken prior to 90 minutes before the performance you attend.\n\nIf you are using the Still App at any other place or time or are not connected to the Still Wifi network, no data will be collected from your device.\n\n\n\nHow we will store and use your data\n•\tThe data will be sent using SSL encryption to a server running in the same  room as the performance. The server will only be visible on the private Still Wifi network and the data will never be transmitted across the internet (only across the private Still Wifi network).\n•\tThe data may be viewed by an employee of TFIU who may select a number of photographs for use during the performance. No photographs containing children or deemed to be private, intimate or containing nudity will be used and they will be immediately deleted. \n•\tTFIU will take action if you any photos appear to be illegal\n•\tIf your photograph is selected for use in performance, it will be shared on a projection screen with the rest of the audience at the performance. Other than this, the data will never be shared with a third party.\n•\tAny collected data will be held on the server for the duration of the performance in which it is collected. Immediately following the performance all the data collected during that performance, along with any copies that were made in order to include it in projections, will be securely deleted (except in the instance of potentially legal images). \n•\tIf you wish to see what data was collected from your device, or if you wish to witness the data being deleted, please request this with TFIU immediately following the performance.\n•\tA TFU employee may cross reference your email address with publicly available data.\n\n\nOpting out\nYou can stop all data collection by disconnecting from the Still Wifi network (in which case no data will be collected or by refusing to accept this privacy policy)\n\nYour consent\nBy using Still App you are consenting to our collecting and use of data as set forth in this Privacy Policy.\n\nContact information\nIf you have any questions about this Privacy Policy, you may contact us at: \ntfiuapp@gmail.com or make yourself known to an employee of TFIU before or after a performance.\n"]]
   [button
    "I agree to the terms" {:on-press #(dispatch [:set-privacy-policy-agreed true])
                            :style {:margin-bottom 50
                                    :margin-top 25}}]])


(defn home-view []
  [view {:style {:flex 1 :alignItems "center"}}
   [text {:style (:header-text styles)} "Still"]

   [button "About Vivian Maier" {:on-press #(dispatch [:nav/push {:key :about :title "About Vivian Maier"}])}]
   [button "Enter show mode" {:on-press #(dispatch [:nav/push {:key :show-mode :title "Show mode"}])}]
   [view {:style {:flex 1 :justify-content "flex-end" :flex-direction "column"}} [text {:style {:color "white" :font-size 10 :text-align "center" :flex 1 :font-family "American Typewriter"}} "Images © Vivian Maier/Maloof Collection,\nCourtesy Howard Greenberg Gallery, New York"]]])

(defn home-screen []
  (let [agreed? (subscribe [:privacy-policy-agreed?])]
    (fn []
      [view {:style (:container styles)}
       [status-bar {:animated true :hidden true}]
       (if @agreed?
         [v/home-view]
         [privacy-policy-view])])))



(def preshow-blurb
  [view {:style {:padding 40}}
  [text {:style (:text styles)}
   "Your device is now in 'show mode'.

Further instructions will be given to you at the beginning of the performance.

At any point before or during the show you may click the icon above to take a photo. Why not practice now, whilst you're waiting?"]])

(defn show-mode []
  (let [show (subscribe [:show])
        awaiting-show? (subscribe [:awaiting-show?])
        camera-authorized? (subscribe [:camera-authorized?])]
    (fn []
      (if @awaiting-show?

        [view preshow-blurb
         (when @camera-authorized?
           [touchable-opacity {:style (:camera-button styles)
                               :on-press #(dispatch [:nav/push {:key :take-picture :title "Take picture"}])}
            [image {:source camera-image}]])]

      (let [{:keys [image-uri message-content]} @show]
        [view {:style (assoc (:container styles)
                        :flex 1
                        :align-items "center"
                        :justify-content "center")}

         [keep-awake] ;; Ensure screen doesn't sleep
         (when image-uri
           [image {:source {:uri image-uri}
                   :style {:width 400 :height 600
                           :resizeMode (.. Image -resizeMode -contain)}}])
         (when @camera-authorized?
           [touchable-opacity {:style (:camera-button styles)
                               :on-press #(dispatch [:nav/push {:key :take-picture :title "Take picture"}])}
            [image {:source camera-image}]])
         (when message-content
           [view {:style (:text-message-box styles)}
            [text {:style (:text-message-heading styles)}
             "Message from H"]
            [image {:source message-icon
                    :style {:width 25 :height 25
                            :position "absolute"
                            :top 5
                            :left 15}}]
            [view {:style {:border-top-width 1
                           :border-color "#666"}}
             [text {:style (:text-message-content styles)}
              message-content]]])
           ])))))

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
      (js/console.log (str "ssid: " @ssid))
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


