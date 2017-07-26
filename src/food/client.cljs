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

(defn destruct [component]
  (let [[tag & other] component]
    (if (even? (count other))
      [tag (apply hash-map other) []]
      [tag (apply hash-map (butlast other)) (last other)])))

(defn view [data]
  [
   ["div" :merge (... (attr "class" "ui top attached topbar menu"))
    [["a" :merge (... (attr "class" "icon item toggle_sidebar"))
      [["i" :merge (... (attr "class" "content icon"))]]]
     ["div" :merge (... (attr "class" "ui transparent icon input"))
      [["input" :merge (... (attr "type" "text")
                            (on "keypress" (fn [] (this-as this
                                                           (->> this
                                                                .-value
                                                                println)))))]
       ["i" :merge (... (attr "class" "search icon"))]]]]]

   ["div" :merge (... (attr "class" "ui bottom attached segment pushable"))
    [["div" :enter (... (attr "class" "ui left inline vertical sidebar menu"))
      (map (fn [c] ["a"
                   :id (t/List-name c)
                   :onexit (...
                            (style "transform" "scaleY(1)")
                            transition
                            (duration 1000)
                            (style "height" "0px")
                            (style "padding-top" "0")
                            (style "padding-bottom" "0")
                            (style "transform" "scaleY(0)"))
                   :onenter (...
                             (each (fn []
                                     (this-as this
                                       (let [self   (.. js/d3 (select this))
                                             height (.. self (style "height"))
                                             top    (.. self (style "padding-top"))
                                             bottom (.. self (style "padding-bottom"))]
                                         (.. self
                                             (style "transform" "scaleY(0)")
                                             (style "padding-bottom" 0)
                                             (style "padding-top" 0)
                                             (style "height" 0)
                                             (transition)
                                             (duration 1000)
                                             (style "height" height)
                                             (style "transform" "scaleY(1)")
                                             (style "padding-bottom" top)
                                             (style "padding-top" bottom)))))))
                   :merge (... (attr "class" "item")
                               (text (fn [] (t/List-name c))))])
           (->> (t/Lists-lists data)))]
     ["div"
      :enter (... (attr "class" "pusher"))
      [["div" :merge (... (attr "class" "ui basic segment"))]]]]]])

(defn render [parent state]
  (let [joined  (.. parent
                    (selectAll #(this-as this
                                         (let [nodes (js/Array.from (aget this "childNodes"))]
                                           (.filter nodes (fn [n] (.-tagName n))))))
                    (data (clj->js state) (fn [d i]
                                            (let [[tag funs children] (destruct d)]
                                              (println (get funs "id"))
                                              (get funs "id" i)))))
        exited  (.. joined exit)

        entered  (.. joined
                     enter
                     (append
                      (fn [d i]
                        (.createElement
                         js/document
                         (let [[tag & _] (destruct d)] tag)))))]
    (.. exited
        (each (fn [d]
                (this-as this
                         (let [self (.. js/d3 (select this))
                               [tag funs children] (destruct d)
                               onexit (or (get funs "onexit") identity)]
                           (.. (onexit self)
                               (on "end" (fn []
                                           (this-as this
                                                    (.. js/d3
                                                        (select this)
                                                        (remove)))))))))))
    (.. entered
        (each (fn [d i]
                (this-as this
                         (let [[tag funs children] (destruct d)
                               draw   (or (get funs "merge") (get funs "enter") identity)
                               onenter (or (get funs "onenter") identity)
                               self (->> (.. js/d3 (select this))
                                         draw
                                         onenter)]
                           (render self children))))))
    (.. joined
        (each (fn [d]
                (let [[tag funs children] (destruct d)
                      draw  (or (get funs "merge") identity)]
                  (this-as this
                           (->> (.. js/d3 (select this))
                                (draw)
                                (#(render %1 children))))))))))

(add-watch
 data :watcher
 (fn [_key _atom _old-state new-state]

   (render (.. js/d3 (select "#app"))
           (view new-state))

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

