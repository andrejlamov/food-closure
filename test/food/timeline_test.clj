(ns food.timeline-test
  (:require  [clojure.test :refer :all]
             [food.timeline :as sut]))

(deftest scratch
  (let [ctx (sut/context)
        ns  :test
        screen (atom "")
        ]

    (swap! ctx assoc-in [ns :enter-exit] [:enter
                                             #(swap! screen str (sut/lookup @ctx ns :somevalue))
                                             :exit])
    (swap! ctx assoc-in [ns :enter]      [#(swap! screen str  "a")])
    (swap! ctx assoc-in [ns :somevalue]  "123")
    (swap! ctx assoc-in [ns :exit]       [#(swap! screen str  "b")])

    (sut/play @ctx)
    (is (= "a123b" @screen))
    )

  )

