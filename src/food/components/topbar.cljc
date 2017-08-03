(ns food.components.topbar
  (:require [food.components.sidebar :as sidebar]
            [food.macros :refer [...]]))

(defn search [client-state text]
  (swap! client-state assoc-in [:search :text] text)
  (swap! client-state assoc-in [:search :store] :Mathem)
  ;; (send  {:operation :Search :client-state @client-state})
  )

(defn create [prefix client-state sidebar-prefix]
  ["div" {:merge (... (attr "class" "ui top attached topbar menu"))}
   ["a" {:merge (... (attr "class" "icon item new_list"))}
    ["i" {:merge (... (attr "class" "add square icon"))}]]
   ["a" {:merge (... (attr "class" "icon item toggle_sidebar")
                     (on "click" #(sidebar/toggle sidebar-prefix client-state)))}
    ["i" {:merge (... (attr "class" "content icon"))}]]
   ["div" {:merge (... (attr "class" "item")
                       (style "display"
                              (if true
                                "" "none"))
                       (text "hello"))
           }]
   ["div" {:merge (... (attr "class" "ui transparent icon input"))}
    ["input" {:merge (... (attr "type" "text")
                          (on "keydown" (fn [] #?(:cljs (this-as this
                                                         (search client-state (.-value this)))
                                                ))))
              }]
    ["i" {:merge (... (attr "class" "search icon"))}]]
   ])
