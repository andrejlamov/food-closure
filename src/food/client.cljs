(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.render :as r :refer [render transform]]
            [food.macros :refer [d3]]))

(enable-console-print!)

(def top-items (atom ["chrome"]))
(def list-items (atom ["chrome" "firefox"]))

(defn root []
  [:div.ui.container

   [:div.ui.top.attached.menu
    (for [n @top-items]
      [:div.ui.icon.item
       {:id (random-uuid)
        :enter (d3 (style "transform" "scaleX(0)")
                   (attr "class" "ui icon item")
                   (transition)
                   (duration 2000)
                   (style "transform" "scaleX(1)")
                   )}
       [:i.icon {:join (d3 (classed n true)
                           (style "opacity" 0)
                           (transition)
                           (delay 2000)
                           (duration 2000)
                           (style "opacity" 1)
                           )}]])]

   [:div.ui.bottom.attached.segment
    (for [n @list-items]
      [:div.ui.vertical.segment
       {:id n
        :click (fn [d] (println n))
        :enter (d3 (style "opacity" 0)
                   (transition)
                   (duration 5000)
                   (style "opacity" 1)
                   (attr "class" "ui vertical segment"))}
        [:div.ui.icon.item>i.huge.icon
         {:join (d3 (classed n true))}]])]])


(defn main []
  (println "client main")
  (render
   (.. js/d3 (select "#app"))
   (root)))

