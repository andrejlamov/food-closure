(ns food.timeline-test
  (:require  [clojure.test :refer :all]
             [food.timeline :as sut]))

(deftest scratch
  (let [t (sut/timelines)
        screen (atom "")
        ]

    (swap! t assoc-in ["enter-exit"] ["enter" #(swap! screen str "123") "exit"])
    (swap! t assoc-in ["enter"]      [#(swap! screen str  "a")])
    (swap! t assoc-in ["exit"]       [#(swap! screen str  "b")])

    (sut/run @t)
    (is (= "a123b" @screen))
    )

  )

