(ns food.timeline-test
  (:require  [clojure.test :refer :all]
             [food.timeline :as sut]))

(deftest scratch
  (let [ctx (sut/context)
        screen (atom "")
        ]

    (swap! ctx assoc-in [:test :enter-exit] [:enter
                                             #(swap! screen str (sut/lookup @ctx :test :somevalue))
                                             :exit])
    (swap! ctx assoc-in [:test :enter]      [#(swap! screen str  "a")])
    (swap! ctx assoc-in [:test :somevalue]  "123")
    (swap! ctx assoc-in [:test :exit]       [#(swap! screen str  "b")])

    (sut/play :test @ctx)
    (is (= "a123b" @screen))
    )

  )

