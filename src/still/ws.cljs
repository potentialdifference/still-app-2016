(ns still.ws
  (:require [re-frame.core :refer [subscribe dispatch]]
            [still.shared :as shared]))

(defn start-websocket-client! [config]
  (let [ws (js/WebSocket. (:ws-host config))]
    (js/console.log "Started websocket...")
    (set! ws.onmessage
          (fn [message]
            (js/console.log (str "Message received:" (.-data message)))
            (let [message (js->clj (js/JSON.parse (.-data message)) :keywordize-keys true)]
              (case (:instruction message)

                "displayText" (dispatch [:display-text (:content message) (:notify message)])
                ;"displayImage"(dispatch [:display-image (str (:public-host config)(:path message))])
                "displayImage"(let [url (str (:public-host config) (:path message))]
                                (shared/download! url {:on-success #(dispatch [:display-image %])
                                                       :on-error #(js/console.log "error " %)}))
                "hideImage" (dispatch [:hide-image])
                "keepAlive" (js/console.log "keepAlive")
                (js/console.log "unknown instruction - doing nothing")))))

    (set! ws.onclose (fn []
                       (js/console.log "Websocket closed!")
                       (js/setTimeout #(start-websocket-client! config) 5000)))
    (set! ws.onerror (fn [err]
                       (js/console.log (str "!!!Websocket error!" (.-message  err)))
                       ))))
