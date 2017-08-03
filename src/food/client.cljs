(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.types :as t]
            [food.macros :as m :refer [...]]))

(enable-console-print!)

(def ws (js/WebSocket. "ws://localhost:3449/ws"))

(defn send [d]
  (->> d
       (prn-str)
       (.send ws)))


(defonce client-data (atom {:sidebar {:visible false}}))
(defonce server-data (atom []))

(defn destruct [[tag funs & children]]
  (js->clj [tag funs (or children [])] :keywordize-keys true))

(defn toggle-sidebar []
  (.. (js/$ "#app .bottom .sidebar")
      (transition (clj->js
                   { :onComplete #(swap! client-data update-in [:sidebar :visible] not true) }))
      (sidebar "toggle")))

(defn sidebar-is-visible []
  (get-in @client-data [:sidebar :visible] false))

(defn set-current-list [list-name]
  (swap! client-data assoc-in [:list :current] list-name))

(defn get-current-list []
  (get-in @client-data [:list :current] nil))

(defn set-candidate-list [candidate-list]
  (swap! client-data assoc-in [:candidate-list] candidate-list))

(defn get-candidate-list []
  (get-in @client-data [:candidate-list] []))

(defn search [text]
  (swap! client-data assoc-in [:search :text] text)
  (swap! client-data assoc-in [:search :store] :Mathem)
  (send  {:operation :Search :client-state @client-data})
  )

(defn view [server-data client-data]
  [
    ["div" {:merge (... (attr "class" "ui top attached topbar menu"))}
     ["a" {:merge (... (attr "class" "icon item toggle_sidebar")
                       (on "click" toggle-sidebar))}
      ["i" {:merge (... (attr "class" "content icon"))}]]
     ["div" {:merge (... (attr "class" "item")
                         (style "display"
                                (if (get-current-list)
                                  "" "none"))
                         (text get-current-list))
            }]
     ["div" {:merge (... (attr "class" "ui transparent icon input"))}
      ["input" {:merge (... (attr "type" "text")
                            (on "keydown" (fn [] (this-as this
                                                  (search (.-value this))
                                                  ))))
                }]
      ["i" {:merge (... (attr "class" "search icon"))}]]
     ]

    ["div" {:merge (... (attr "class" "ui bottom attached segment pushable"))}
     ["div" {:merge (... (attr "class" "ui left vertical sidebar menu")
                         (classed "visible" sidebar-is-visible))}
      ;; (for [l
      ;;       (->> (t/Lists-lists server-data))
      ;;       ]
      ;;   ["a" {:merge (... (attr "class" "item")
      ;;                     (text (fn [] (t/List-name l)))
      ;;                     (on "click" (fn []
      ;;                                   (set-current-list (t/List-name l)))
      ;;                                   ))
      ;;         :id (t/List-name l)
      ;;         :onexit (... (style "transform" "scaleY(1)")
      ;;                      transition
      ;;                      (duration 1000)
      ;;                      (style "height" "0px")
      ;;                      (style "padding-top" "0")
      ;;                      (style "padding-bottom" "0")
      ;;                      (style "transform" "scaleY(0)"))
      ;;         :onenter (...
      ;;                   (each (fn []
      ;;                           (this-as this
      ;;                             (let [self   (.. js/d3 (select this))
      ;;                                   height (.. self (style "height"))
      ;;                                   top    (.. self (style "padding-top"))
      ;;                                   bottom (.. self (style "padding-bottom"))]
      ;;                               (.. self (style "transform" "scaleY(0)")
      ;;                                   (style "padding-bottom" 0)
      ;;                                   (style "padding-top" 0)
      ;;                                   (style "height" 0)
      ;;                                   transition
      ;;                                   (duration 1000)
      ;;                                   (style "height" height)
      ;;                                   (style "transform" "scaleY(1)")
      ;;                                   (style "padding-bottom" top)
      ;;                                   (style "padding-top" bottom)))))))}])

      ]
     ["div" {:merge (... (attr "class" "pusher")
                         (classed "dimmed" sidebar-is-visible)
                         (on "click" (fn [] (when (sidebar-is-visible)
                                             (toggle-sidebar)))))}
      ["div" {:merge (... (attr "class" "ui basic segment"))}
       (for [{:keys [image title]} (get-in client-data [:search-result :list])]
         ["img" {
                 :id title
                 :merge (... (attr "src" image))
                 }
          ]
         )
       ]]]])

(defn children-in-collection? [children]
  (if (= '(()) children)
    true
    (let [[[head & _] :as child & other] children]
      (vector? head))))

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
                          onexit (or onexit (... remove))]
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
 (fn [_key _atom _old-state server-data]

   (render (.. js/d3 (select "#app"))
           (view server-data @client-data))

      ))

(add-watch
 client-data :watcher
 (fn [_key _atom _old-state client-data]
   (println "*** client state")
   (println client-data)
   (render (.. js/d3 (select "#app"))
           (view @server-data client-data))

   ))

(defn evaluate [data]
  (doall (for [[path value] data]
           (swap! client-data assoc-in path value)
           )))

(defn main []
  (println "client main")
  (render (.. js/d3 (select "#app"))
          (view @server-data @client-data))
  (.. (js/$ ".sidebar")
      (sidebar (clj->js
                {:context (js/$ "#app .ui.bottom.segment")}))
      (sidebar "setting" "transition" "overlay"))

  )

(set! (.-onopen ws) (fn []
                      (send {:operation :Subscribe :client-state nil})
                      (main)))

(defn log [a]
  (println a)
  a
  )

(set! (.-onmessage ws) (fn [server-data]
                         (->> (.-data server-data)
                              (cljs.reader/read-string)
                              :msg
                              (evaluate))))

