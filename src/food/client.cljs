(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.render :as r :refer [render transform]]
            [food.macros :refer [d3]]))

(enable-console-print!)

(def top-items (atom [
                      "firefox"
                      "chrome"
                      "opera"
                      ]))
(def list-items (atom [
                       ;; "firefox"
                       ;; "opera"
                       ;; "chrome"
                       ]))

(defn pos [e]
    (let [o (.. (js/$ e) offset)]
      [(.-top o) (.-left o)]))

(defn root []
  [:div.ui.container

   [:div.ui.top.attached.menu
    (for [n @top-items]
      [:div.ui.icon.item
       {:id n
        :enter (d3 (each (fn [] (this-as this
                                 (let [
                                       self (.. js/d3 (select this)
                                                (attr "class" "ui icon item"))
                                       lp (.. self (style "padding-left"))
                                       rp (.. self (style "padding-right"))
                                       p  (.. self (style "padding"))
                                       ]
                                   (.. self
                                       (style "height" "6em")
                                       (style "padding-left" 0)
                                       (style "padding-right" 0)
                                       (style "width" 0)
                                       (transition)
                                       (duration 4000)
                                       (style "transform" "scaleX(1)")
                                       (style "padding-left" lp)
                                       (style "padding-right" rp)
                                       (style "width" "6em")
                                       )
                                   )
                                 )))
                )}
       [:i.huge.icon {
                      :join (d3 (classed n true)
                                (classed (str "topbar-" n) true)
                                )
                      :enter (d3 (each (fn []
                                         (this-as this
                                           (let [self (.. js/d3 (select this))
                                                 ]
                                             (.. self
                                                 (style "opacity" 0)
                                                 (attr "class" "huge icon")
                                                 (classed n true)
                                                 (classed (str "topbar-" n) true)
                                                 (transition)
                                                 (delay 3000)
                                                 (transition)
                                                 (duration 2000)
                                                 (style "opacity" 1)))))))

                      }]])]

   [:div.ui.bottom.attached.segment
    (for [n @list-items]
      [:div.ui.vertical.segment
       {:id n
        :click (fn [d] (println n))
        :enter (d3 (style "opacity" 0)
                   ;; (style "color" "green")
                   (transition)
                   (duration 2000)
                   (style "opacity" 1)
                   (attr "class" "ui vertical segment"))
        :exit  (d3
                (transition)
                (delay 2000)
                (on "end"
                    (fn []
                      (this-as this
                        (let [self         (.. js/d3 (select this))
                              subject      (.. self (select "i.icon"))
                              [t1 l1] (pos (str "i.topbar-" n))
                              [t0 l0] (pos (.. subject node))
                              ]
                          (println [t1 l1])
                          (println [t0 l0])
                          (.. self
                              (style "opacity" 1)
                              ;; (style "color" "red")
                              (classed "exit" true)
                              (transition)
                              (delay 2000)
                              (remove)
                              )
                          (.. subject
                              (transition)
                              (duration 2000)
                              (style "transform" (str "translate(" (- (- l0 l1)) "px," (- (- t0 t1)) "px)"))
                              )

                          )))))}
       [:div.ui.icon.item>i.huge.icon
        {:join (d3
                (classed n true))}]])]])

(defn main []
  (println "client main")
  (render
   (.. js/d3 (select "#app"))
   (root)))

