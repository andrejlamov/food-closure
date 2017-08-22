(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.render :as r :refer [render transform]]
            [food.macros :refer [d3]]))

(enable-console-print!)

(defn root []
  [:div.ui.container>div.ui.segment {:merge (d3 (style "background-color" "red"))}
   (for [s ["hello" "world"]]
     [:div.ui.vertical.segment
      {:enter (d3 (style "opacity" 0)
                  (transition)
                  (duration 5000)
                  (style "opacity" 1)
                  (attr "class" "ui vertical segment"))}
      [:h2
       {:merge (d3 (style "color" "blue")
                   (text s))}]])])

(defn main []
  (println "client main")
  (render
   (.. js/d3 (select "#app"))
   (root)))

