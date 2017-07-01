(ns food.eval-test
  (:require [food.eval :as sut]
            [clojure.test :as t]))

(t/deftest manipulate-atom-set
  (let [hub (atom #{1})]
    (sut/subscribe 2 hub)
    (t/is (= #{1 2} @hub))

    (sut/unsubscribe 2 hub)
    (t/is (= #{1} @hub))
    )
  )

