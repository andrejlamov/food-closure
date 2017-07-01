(ns food.systemtest
  (:require
   [food.types :as t]
   [food.eval :as e]
   [food.util :as u]
   [food.mathem :as mathem]))

(def data-states (atom [{}]))
(def scope (u/scope nil [] data-states))

(defn doit []
    (let [
          candidates  (e/evaluate (t/SearchQuery "lingongrova" :mathem) scope)
          lingongrova (-> candidates
                          (t/CandidateList-items)
                          (first))
          _            (e/evaluate (t/CreateList "food") scope)
          _            (e/evaluate (t/AddItem "food" lingongrova) scope)
          ]
      )
    )

(doit)
@data-states
