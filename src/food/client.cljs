(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.types :as t]
            [food.macros :as m :refer [...]]))

(enable-console-print!)

(def ws (js/WebSocket. "ws://localhost:3449/ws"))

(def client-data (atom {:sidebar {:visible true}}))
(def server-data (atom []))

(add-watch
 client-data :watcher
 (fn [_key _atom _old-state new-state]
   (println "*** client state")
   (println new-state)))

(defn destruct [[tag funs & children]]
  (js->clj [tag funs (or children [])] :keywordize-keys true))

(defn toggle-sidebar []
  (.sidebar (js/$ "#app .bottom .sidebar") "toggle")
  (swap! client-data update-in [:sidebar :visible]  not true))

(defn sidebar-is-visible []
  (get-in @client-data [:sidebar :visible] false))

(defn view [server-data client-data]
  [
    ["div" {:merge (... (attr "class" "ui top attached topbar menu"))}
     ["a" {:merge (... (attr "class" "icon item toggle_sidebar")
                       (on "click" toggle-sidebar))}
      ["i" {:merge (... (attr "class" "content icon"))}]]

     ["div" {:merge (... (attr "class" "ui transparent icon input"))}
      ["input" {:merge (... (attr "type" "text")
                            (on "keypress" (fn [] (this-as this
                                                   (->> this
                                                        .-value
                                                        println)))))}]
      ["i" {:merge (... (attr "class" "search icon"))}]] ]

    ["div" {:merge (... (attr "class" "ui bottom attached segment pushable"))}
     ["div" {:merge (... (attr "class" "ui left inline vertical sidebar menu")
                         (classed "visible" sidebar-is-visible))}
      (for [l (t/Lists-lists server-data)]
        ["a" {:merge (... (attr "class" "item")
                          (text (fn [] (t/List-name l))))
              :id (t/List-name l)
              :onexit (... (style "transform" "scaleY(1)")
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
                                    (.. self (style "transform" "scaleY(0)")
                                        (style "padding-bottom" 0)
                                        (style "padding-top" 0)
                                        (style "height" 0)
                                        transition
                                        (duration 1000)
                                        (style "height" height)
                                        (style "transform" "scaleY(1)")
                                        (style "padding-bottom" top)
                                        (style "padding-top" bottom)))))))}])]
     ["div" {:merge (... (attr "class" "pusher")
                         (classed "dimmed" sidebar-is-visible)
                         (on "click" (fn [] (when (sidebar-is-visible)
                                             (toggle-sidebar)))))}
      ["div" {:merge (... (attr "class" "ui basic segment"))}]]]])

(defn children-in-collection? [children]
  (let [[[head & _] :as child & other] children]
    (vector? head)))

(defn render [parent children]
  (if (children-in-collection? children)
    (render parent (first children))
    (let [joined  (.. parent
                      (selectAll #(this-as this
                                    (let [nodes (js/Array.from (aget this "childNodes"))]
                                      (.filter nodes (fn [n] (.-tagName n))))))
                      (data (clj->js children) (fn [d i]
                                                 (let [[tag {:keys [id]} children] (destruct d)]
                                                   (or id i)))))
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
                          [tag {:keys [onexit]} children] (destruct d)
                          onexit (or onexit identity)]
                      (.. (onexit self)
                          (on "end" (fn []
                                      (this-as this
                                        (.. js/d3
                                            (select this)
                                            (remove)))))))))))
      (.. entered
          (each (fn [d i]
                  (this-as this
                    (let [[tag {:keys [merge enter onenter]} children] (destruct d)
                          ;; TODO: enter should have prio over merge
                          draw   (or merge enter identity)
                          onenter (or onenter identity)
                          self (->> (.. js/d3 (select this))
                                    draw
                                    onenter)]
                      (render self children))))))
      (.. joined
          (each (fn [d]
                  (let [[tag {:keys [merge]} children] (destruct d)
                        draw  (or merge identity)]
                    (this-as this
                      (->> (.. js/d3 (select this))
                           (draw)
                           (#(render %1 children)))))))))))

(add-watch
 server-data :watcher
 (fn [_key _atom _old-state new-state]

   (render (.. js/d3 (select "#app"))
           (view new-state @client-data))

   (.. (js/$ ".sidebar")
       (sidebar (clj->js
                 {:context (js/$ "#app .ui.bottom.segment")}))
       (sidebar "setting" "transition" "overlay"))
   ))

(defn send [d]
  (->> d
       (prn-str)
       (.send ws)))

(defmulti evaluate m/get-type)
(defmethod evaluate :CandidateList [d]
  (->>
   d
   (t/CandidateList-items)
   (first)
   (t/AddItem "nuts")
   (send)))

(defmethod evaluate :Lists [new-data]
  (reset! server-data new-data))

(defn main []
  (send (t/AllLists))
  ;; (send (t/SearchQuery "rostade mandlar" (t/Mathem)))
)
(set! (.-onopen ws) (fn []
                      (send (t/Subscribe))
                      (main)))

(set! (.-onmessage ws) (fn [server-data]
                         (->> (.-data server-data)
                              (cljs.reader/read-string)
                              (evaluate))))
