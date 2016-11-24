(ns still.ws
  (:require [re-frame.core :refer [subscribe dispatch]]))

(defn start-websocket-client! [config]
  (let [ws (js/WebSocket. (:ws-host config))]
    (js/console.log "Started websocket...")
    (set! ws.onmessage
          (fn [message]
            (js/console.log (str "Message received:" (.-data message)))
            (let [message (js->clj (js/JSON.parse (.-data message)) :keywordize-keys true)]
              (case (:instruction message)
                "displayText" (dispatch [:display-text (:content message)])
                "displayImage" (dispatch [:display-image (str (:public-host config)
                                                              (:path message))])
                "hideImage" (dispatch [:hide-image])
                "keepAlive" (js/console.log "keepAlive")
                (js/console.log "unknown instruction - doing nothing")))))

    (set! ws.onclose (fn []
                       (js/console.log "Websocket closed!")
                       (js/setTimeout #(start-websocket-client! config) 5000)))
    (set! ws.onerror (fn [err]
                       (js/console.log (str "!!!Websocket error!" (.-message  err)))
                       ))))
