(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.components.sidebar :as sidebar]
            [food.components.topbar :as topbar]
            [food.types :as t]
            [food.render :refer [render transform]]
            [food.macros :as m :refer [d3]]))

(enable-console-print!)

(def ws (js/WebSocket. "ws://localhost:3449/ws"))

(defn send [d]
  (->> d
       (prn-str)
       (.send ws)))


(def client-state (atom (merge {}
                               (sidebar/initial-state :sidebar)
                               )))

(defonce server-state (atom []))

(defn set-current-list [list-name]
  (swap! client-state assoc-in [:list :current] list-name))

(defn get-current-list []
  (get-in @client-state [:list :current] nil))

(defn set-candidate-list [candidate-list]
  (swap! client-state assoc-in [:candidate-list] candidate-list))

(defn get-candidate-list []
  (get-in @client-state [:candidate-list] []))

(defn root []
  [
   [:div.ui.container]
   ])

;; (add-watch
;;  server-state :watcher
;;  (fn [_key atom _old-state server-state]

;;    (render (.. js/d3 (select "#app"))
;;            (root atom client-state))

;;       ))

;; (add-watch
;;  client-state :watcher
;;  (fn [_key atom _old-state client-state]
;;    (println "*** client state")
;;    (println client-state)
;;    (render (.. js/d3 (select "#app"))
;;            (root server-state atom))

;;    ))

(defn evaluate [data]
  (doall (for [[path value] data]
           (swap! client-state assoc-in path value)
           )))

(defn main []
  (println "client main")
  (println (map transform (root)))
  (render (.. js/d3 (select "#app"))
          (map transform (root)))
  ;; (.. (js/$ ".sidebar")
  ;;     (sidebar (clj->js
  ;;               {:context (js/$ "#app .ui.bottom.segment")}))
  ;;     (sidebar "setting" "transition" "overlay"))

  )

(set! (.-onopen ws) (fn []
                      (send {:operation :Subscribe :client-state nil})
                      (main)))

(defn log [a]
  (println a)
  a
  )

(set! (.-onmessage ws) (fn [server-state]
                         (->> (.-data server-state)
                              (cljs.reader/read-string)
                              :msg
                              (evaluate))))

