(ns food.timeline-test
  (:require  [clojure.test :refer :all]
             [food.timeline :as sut]))

(deftest scratch
  (testing "override enter and exit with override if all three are defined in ctx"
    (let [ctx (sut/context)
          ns  :test
          screen (atom "")]
        (sut/add-override ctx ns [:enter
                                  (fn [cb] (swap! screen str "123") (cb))
                                  :exit])
        (sut/add-enter ctx ns (fn [cb] (swap! screen str "a") (cb)))
        (sut/add-exit ctx ns (fn [cb] (swap! screen str "b") (cb)))


        (is (sut/play @ctx))
        (is (= "a123b" @screen))))

  (testing "play enter and exit if override can not be played"
    (let [ctx (sut/context)
          ns  :test
          screen (atom "")]

      (sut/add-override ctx ns [:enter
                                (fn [cb] (swap! screen str "hello" (cb)))
                                :exit])
      (sut/add-exit ctx ns (fn [cb] (swap! screen str  "b") (cb)))

      (sut/play @ctx)

      (is (= "b" @screen)))))
