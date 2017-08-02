(ns food.hub_test
  (:require [food.hub :as sut]
            [clojure.test :refer :all]))

(deftest channels-test
  (let [{:keys [channels] :as hub} (sut/construct-channel-hub)]
    (testing "construct empty hub"
      (is (empty? @channels)))

    (testing "subscribe"
      (is (= #{1}   (sut/subscribe hub 1)))
      (is (= #{1 2} (sut/subscribe hub 2))))

    (testing "single send"
      (is (= {:channel 1 :msg "hello"} (sut/send 1 "hello"))))

    (testing "publish to all"
      (is (= #{{:channel 1 :msg "hello"}
               {:channel 2 :msg "hello"}}
             (set (sut/publish hub "hello")))))))

