(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [food.types :as t]
            :reload))

(enable-console-print!)

(defn log [& args]
  (println "*** cljs: " args)
  args)

(def ws (js/WebSocket. "ws://localhost:3449/ws"))

(defn send [d]
  (->> d
       (prn-str)
       (.send ws)))


(defn main []
  (log "main!")
  (send (t/SearchQuery "lingongrova" (t/Mathem)))
  )

(set! (.-onopen ws) (fn []
                      (send (t/Subscribe))
                      (log "open")))

(set! (.-onmessage ws) (fn [data]
                         (-> (.-data data)
                             (cljs.reader/read-string)
                             log)))
