(ns food.channels_test
  (:require [food.channels :as sut]
            [clojure.test :as t]))

(t/use-fixtures :each (fn [f]
                        (reset! sut/hub #{})
                        (f)))

(t/deftest channels-test

  (sut/subscribe 1)

  (sut/subscribe 2)

  (t/is (= [{:channel 1 :msg "hello"}
            {:channel 2 :msg "hello"}] (sut/publish-to-all "hello")))

  (sut/unsubscribe 1)

  (t/is (= #{2} @sut/hub)))

