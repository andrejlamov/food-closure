(ns food.timeline-test
  (:require  [clojure.test :refer :all]
             [food.timeline :as sut]))

(deftest scratch
  (testing "override enter and exit with enter-exit if all three are defined in ctx"
    (let [ctx (sut/context)
          ns  :test
          screen (atom "")]
        (sut/add ctx ns :enter-exit [:enter
                                     (fn [cb] (swap! screen str (sut/lookup @ctx ns :somevalue)) (cb))
                                     :exit])
        (sut/add ctx ns :enter [(fn [cb] (swap! screen str "a") (cb))])
        (sut/add ctx ns :somevalue  "123")
        (sut/add ctx ns :exit  [(fn [cb] (swap! screen str "b") (cb))])


        (sut/play @ctx)
        (is (= "a123b" @screen))))

  (testing "play enter and exit if enter-exit can not be played"
    (let [ctx (sut/context)
          ns  :test
          screen (atom "")]

      (sut/add ctx ns :enter-exit [:enter
                                   (fn [cb] (swap! screen str (sut/lookup @ctx ns :somevalue) (cb)))
                                   :exit])
      (sut/add ctx ns :somevalue  "123")
      (sut/add ctx ns :exit  [(fn [cb] (swap! screen str  "b") (cb))])

      (sut/play @ctx)

      (is (= "b" @screen)))))
