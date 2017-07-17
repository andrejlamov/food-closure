(ns food.systemtest
  (:require
   [food.types :refer :all ]
   [food.eval :as e]
   [food.util :as u]
   )
  )

(def scope (Scope nil [] "test-db"))

(defn doit []
    (let [
          candidates  (e/evaluate (SearchQuery "lingongrova" (Mathem)) scope)
          lingongrova (-> candidates
                          (CandidateList-items)
                          (first))
          _            (e/evaluate (CreateList "food") scope)
          _            (e/evaluate (AddItem "food" lingongrova) scope)
          ]
      )
  )

(doit)
;; (last @data-states)
