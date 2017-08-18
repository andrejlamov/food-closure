(ns food.components.topbar
  (:require [food.components.sidebar :as sidebar]
            [food.macros :refer [d3]]))

(defn search [client-state text]
  (swap! client-state assoc-in [:search :text] text)
  (swap! client-state assoc-in [:search :store] :Mathem)
  ;; (send  {:operation :Search :client-state @client-state})
  )

(defn create [prefix client-state sidebar-prefix]
  ["div" {:merge (d3 (attr "class" "ui top attached topbar menu"))}
   ["a" {:merge (d3 (attr "class" "icon item new_list"))}
    ["i" {:merge (d3 (attr "class" "add square icon"))}]]
   ["a" {:merge (d3 (attr "class" "icon item toggle_sidebar")
                     (on "click" #(sidebar/toggle sidebar-prefix client-state)))}
    ["i" {:merge (d3 (attr "class" "content icon"))}]]
   ["div" {:merge (d3 (attr "class" "item")
                       (style "display"
                              (if true
                                "" "none"))
                       (text "hello"))
           }]
   ["div" {:merge (d3 (attr "class" "ui transparent icon input"))}
    ["input" {:merge (d3 (attr "type" "text")
                          (on "keydown" (fn [] #?(:cljs (this-as this
                                                         (search client-state (.-value this)))
                                                ))))
              }]
    ["i" {:merge (d3 (attr "class" "search icon"))}]]
   ])
