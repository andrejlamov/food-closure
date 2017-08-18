(ns food.components.sidebar
  (:require [food.macros :refer [d3]]
            #?(:cljs [cljsjs.jquery])
            ))

(defn toggle [prefix client-state]
  (println  "cl*")
  #?(:cljs (.. (js/$ "#app .bottom .sidebar")
               (transition (clj->js
                            { :onComplete #(swap! client-state update-in [prefix :visible] not true) }))
               (sidebar "toggle"))))

(defn is-visible [prefix client-state]
  (get-in @client-state [prefix :visible] false))

(defn initial-state [prefix]
  {prefix {:visible true}})

(defn create [prefix client-state server-state]
  ["div" {:merge (d3 (attr "class" "ui left vertical sidebar menu")
                      (classed "visible" (get-in @client-state [prefix :visible])))}
      ;; (for [l
      ;;       (->> (t/Lists-lists server-data))
      ;;       ]
      ;;   ["a" {:merge (d3 (attr "class" "item")
      ;;                     (text (fn [] (t/List-name l)))
      ;;                     (on "click" (fn []
      ;;                                   (set-current-list (t/List-name l)))
      ;;                                   ))
      ;;         :id (t/List-name l)
      ;;         :onexit (d3 (style "transform" "scaleY(1)")
      ;;                      transition
      ;;                      (duration 1000)
      ;;                      (style "height" "0px")
      ;;                      (style "padding-top" "0")
      ;;                      (style "padding-bottom" "0")
      ;;                      (style "transform" "scaleY(0)"))
      ;;         :onenter (d3
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
)
  
