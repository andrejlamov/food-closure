(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [cljs.core.async :as async
             :refer [unsub sub pub <! >! chan close! sliding-buffer put! alts!]]
            [food.render :as r :refer [render transform]]
            [food.macros :refer [d3]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def state (atom {:top-items []

                  :list-items ["chrome"
                               "firefox"
                               "safari"
                               "edge"
                               "opera"]}))

(defn pos [e]
  (let [o (.. (js/$ e) offset)]
    [(.-top o) (.-left o)]))

(def input-chan (chan))
(def our-pub (pub input-chan :topic))

(defn root []
  [:div.ui.container
   [:div.ui.top.attached.menu {:join (d3 (style "height" "6em"))}
    (for [n (:top-items @state)]
      [:div.ui.icon.item
       {:id n
        :enter (d3 (each (fn []
                           (println "enter" n)
                           (this-as this
                                    (let [output-chan (chan)
                                          self (.. js/d3 (select this))
                                          lp   (.. self (style "padding-left"))
                                          rp   (.. self (style "padding-right"))]
                                      (.. self
                                          (style "padding-left" 0)
                                          (style "padding-right" 0)
                                          (style "width" 0))
                                      (sub our-pub (str "start/topbar-icon-slot/" n) output-chan)
                                      (go-loop [] (<! output-chan)
                                               (.. js/d3 (select this)
                                                   transition
                                                   (duration 2000)
                                                   (style "width" "6em")
                                                   (style "padding-left" lp)
                                                   (style "padding-right" rp)
                                                   (on "end" (fn []
                                                               (println "will send end slot" n)
                                                               (let [p (pos (.. js/d3
                                                                                (select this)
                                                                                (select "i")
                                                                                node))]
                                                                 (put! input-chan
                                                                       {:topic (str "end/topbar-icon-slot/" n)
                                                                        :position p})))))))))))}
       [:i.huge.icon {:id n
                      :join (d3 (classed n true))
                      :enter (d3 (each (fn [] (this-as this
                                                       (let [output-chan (chan)
                                                             self (.. js/d3 (select this))]
                                                         (.. self
                                                             (style "opacity" 0))
                                                         (sub our-pub (str "display/topbar-icon/" n) output-chan)
                                                         (go-loop [] (<! output-chan)
                                                                  (.. js/d3 (select this)
                                                                      (style "opacity" 1))))))))}]])]

   [:div.ui.bottom.attached.segment
    (for [n (:list-items @state)]
      [:div.ui.vertical.segment
       {:id n
        :click (fn [d]

                 (println "click" n)
                 (swap! state update-in [:list-items] (partial remove #{n}))
                 (swap! state update-in [:top-items] conj n)
                 (println "state" @state)
                 (render
                  (.. js/d3 (select "#app"))
                  (root)))

        :enter (d3 (style "opacity" 0)
                   (transition)
                   (style "opacity" 1)
                   (attr "class" "ui vertical segment"))
        :exit  (d3 (each (fn [] (this-as this
                                         (println "exit" n)
                                         (put! input-chan {:topic (str "start/topbar-icon-slot/" n)})
                                         (let [output-chan (chan)]
                                           (sub our-pub (str "end/topbar-icon-slot/" n) output-chan)
                                           (go-loop []
                                             (let [{:keys [position]} (<! output-chan)
                                                   [t1 l1] position
                                                   [t0 l0] (pos (.. js/d3 (select this) (select "i") node))]
                                               (.. js/d3 (select this)
                                                   (select "i")
                                                   (transition)
                                                   (duration 2000)
                                                   (style "transform" (str "translate(" (- (- l0 l1)) "px," (- (- t0 t1)) "px)"))
                                                   (on "end" (fn []
                                                               (println "will remove" n)
                                                               (put! input-chan {:topic (str "display/topbar-icon/" n)})
                                                               (.. js/d3 (select this)
                                                                   remove)))))))))))}
       [:div.ui.icon.item>i.huge.icon
        {:join (d3
                (classed n true))}]])]])

(defn main []
  (println "client main")
  (render
   (.. js/d3 (select "#app"))
   (root)))
