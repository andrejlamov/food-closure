(ns food.hub_test
  (:require [food.hub :as sut]
            [clojure.test :refer :all]))

(deftest output-queue-limit-test
  (let [{:keys [output-queue] :as hub} (sut/construct-channel-hub {:output-limit 3})
        channel "0.0.0.0:123"
        ]
    (testing "return last three messages"
      (sut/send hub channel 1)
      (sut/send hub channel 2)
      (is (= [
              {:channel channel :msg 1}
              {:channel channel :msg 2}
              {:channel channel :msg 3}
              ] (sut/send hub channel 3) @output-queue))
      (sut/send hub channel 4)
      (is (= [
              {:channel channel :msg 3}
              {:channel channel :msg 4}
              {:channel channel :msg 5}
              ] (sut/send hub channel 5) @output-queue))
      )
    )
  )

(deftest channels-test
  (let [{:keys [channels output-queue output-limit] :as hub} (sut/construct-channel-hub)]

    (testing "output limit is 1 by default"
      (is (empty? @channels))
      (is (= output-limit 1)))

    (testing "subscribe"
      (is (= #{1}   (sut/subscribe hub 1)))
      (is (= #{1 2} (sut/subscribe hub 2))))

    (testing "single send"
      (is (= [{:channel 1 :msg "hi"}] (sut/send hub 1 "hi"))))

    (testing "publish to all"
      (is (= #{[{:channel 1 :msg "hello"}]
               [{:channel 2 :msg "hello"}]}
             (set (sut/publish hub "hello")))))))

