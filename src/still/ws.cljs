(ns still.ws
  (:require [re-frame.core :refer [subscribe dispatch]]))

(defn start-websocket-client! [config]
  (let [ws (js/WebSocket. (:ws-host config))]
    (js/console.log "Started websocket...")
    (set! ws.onmessage
          (fn [message]
            (let [message (js->clj (js/JSON.parse (.-data message)) :keywordize-keys true)]
              (case (:instruction message)
                "displayText" (dispatch [:display-text (:content message)])
                "displayImage" (dispatch [:display-image (str (:public-host config)
                                                              (:path message))])
                "hideImage" (dispatch [:hide-image])))))
    (set! ws.onclose (fn []
                       (js/console.log "Websocket closed!")
                       (js/setTimeout #(start-websocket-client! config) 5000)))))
