(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.types :as t]
            [food.macros :as m :refer [...]]))

(enable-console-print!)

(def ws (js/WebSocket. "ws://localhost:3449/ws"))

(def data (atom []))

(defn log [& args]
  (println "*** cljs: " args)
  args)



(defn view-state [data]
  [{:merge (... (attr "class" "ui top attached topbar menu"))
    :element "div"
    :children [{:element "a"
                :id "toggle menu"
                :merge (... (attr "class" "icon item toggle_sidebar"))
                :children [{:element "i"
                            :merge (... (attr "class" "content icon"))}]}
               {:element "div"
                :merge (... (attr "class" "ui transparent icon input"))
                :children [{:element "input"
                            :merge (... (attr "type" "text"))}
                           {:element "i"
                            :merge (... (attr "class" "search icon"))}]}]}

   {:element "div"
    :merge (... (attr "class" "ui bottom attached segment pushable"))
    :children [{:enter (... (attr "class" "ui left inline vertical sidebar menu"))
                :element "div"
                :children (map (fn [c]
                                 {:element "a"
                                  :id (t/List-name c)
                                  :animate_enter "slide right"
                                  :animate_exit "slide right"
                                  :merge (... (attr "class" "item")
                                              (text (fn [] (t/List-name c))))
                                  })
                               (->> (t/Lists-lists data)))}

               {:element "div"
                :enter (... (attr "class" "pusher"))
                :children [{:element "div"
                            :merge (... (attr "class" "ui basic segment"))}]}]}])

(defn render [parent state]
  (when (not (empty? state))
    (let [joined  (.. parent
                      (selectAll #(this-as this
                                    (let [nodes (aget this "childNodes")]
                                      nodes)))
                      (data (clj->js state) (fn [d i]
                                              (or (.-id d) i))))
          exited  (.. joined exit)

          entered  (.. joined
                       enter
                       (append
                        (fn [d i]
                          (.createElement
                           js/document
                           (.-element d)))))]
      (.. exited
          (each (fn [d]
                  (if (and d (.-animate_exit d))
                    (this-as this
                      (.transition (js/$ this)
                                   (clj->js
                                    {:animation (.-animate_exit d)
                                     :onComplete #(.. exited remove)})))
                    (.. exited remove)))))

      (.. entered
          (each (fn [d i]
                  (this-as this
                    (let [draw   (or (.-merge d) (.-enter d) identity)
                          children  (or (.-children d) [])
                          entered (->> (.. js/d3
                                           (select this))
                                       (draw)
                                       )]
                      (when (.-animate_enter d)
                        (.. entered
                            (classed "transition hidden" true))
                        (.transition (js/$ this)
                                     (clj->js {:animation (.-animate_enter d)})))
                      (render entered children)
                      )))))
      (.. joined
          (each (fn [d]
                  (let [draw  (or (.-merge d) identity)
                        children (or (.-children d) [])]
                    (this-as this
                      (->> (.. js/d3 (select this))
                           (draw)
                        (#(render %1 children)))))))))))

(add-watch
 data :watcher
 (fn [_key _atom _old-state new-state]

   (render (.. js/d3 (select "#app"))
           (view-state new-state))

   (.. (js/$ ".sidebar")
       (sidebar (clj->js
                 {:context (js/$ "#app .ui.bottom.segment")}))
       (sidebar "setting" "transition" "overlay")
       (sidebar "attach events" "#app .topbar .item.toggle_sidebar"))))

(defn send [d]
  (->> d
       (prn-str)
       (.send ws)))

(defmulti evaluate m/get-type)
(defmethod evaluate :CandidateList [d]
  ;; (println (first d))
  (->>
   d
   (t/CandidateList-items)
   (first)
   (t/AddItem "nuts")
   (send)))

(defmethod evaluate :Lists [new-data]
  (reset! data new-data))

(defn main []
  (log "main!")
  (send (t/AllLists))
  ;; (send (t/SearchQuery "rostade mandlar" (t/Mathem)))
)
(set! (.-onopen ws) (fn []
                      (send (t/Subscribe))
                      (log "open")))

(set! (.-onmessage ws) (fn [data]
                         (->> (.-data data)
                              (cljs.reader/read-string)
                              (evaluate))))

;; (.. js/d3 (select "body")
;;     (attr "class" "3"))

;; ((...
;;   (attr "class" "2")
;;   (attr "style" "asd3")
;;   ) (.. js/d3 (select "body")))

