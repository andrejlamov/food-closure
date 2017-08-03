(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.components.sidebar :as sidebar]
            [food.components.topbar :as topbar]
            [food.types :as t]
            [food.macros :as m :refer [...]]))

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

(defn destruct [[tag funs & children]]
  (js->clj [tag funs (or children [])] :keywordize-keys true))

(defn set-current-list [list-name]
  (swap! client-state assoc-in [:list :current] list-name))

(defn get-current-list []
  (get-in @client-state [:list :current] nil))

(defn set-candidate-list [candidate-list]
  (swap! client-state assoc-in [:candidate-list] candidate-list))

(defn get-candidate-list []
  (get-in @client-state [:candidate-list] []))

(defn view [server-state client-state]
  [
   (topbar/create :topbar client-state :sidebar)
    ["div" {:merge (... (attr "class" "ui bottom attached segment pushable"))}
     (sidebar/create :sidebar client-state server-state)
     ["div" {:merge (... (attr "class" "pusher")
                         (classed "dimmed" #(sidebar/is-visible :sidebar client-state))
                         (on "click" #(when (sidebar/is-visible :sidebar client-state)
                                        (sidebar/toggle :sidebar client-state))))}
      ["div" {:merge (... (attr "class" "ui basic segment"))}
       (for [{:keys [image title]} (get-in @client-state [:search-result :list])]
         ["img" {
                 :id title
                 :merge (... (attr "src" image))
                 }
          ]
         )
       ]

      ]]])

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
 server-state :watcher
 (fn [_key atom _old-state server-state]

   (render (.. js/d3 (select "#app"))
           (view atom client-state))

      ))

(add-watch
 client-state :watcher
 (fn [_key atom _old-state client-state]
   (println "*** client state")
   (println client-state)
   (render (.. js/d3 (select "#app"))
           (view server-state atom))

   ))

(defn evaluate [data]
  (doall (for [[path value] data]
           (swap! client-state assoc-in path value)
           )))

(defn main []
  (println "client main")
  (render (.. js/d3 (select "#app"))
          (view server-state client-state))
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

(set! (.-onmessage ws) (fn [server-state]
                         (->> (.-data server-state)
                              (cljs.reader/read-string)
                              :msg
                              (evaluate))))

