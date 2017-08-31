(ns food.animation-test
  (:require  [clojure.test :refer :all]
             [food.animation :as sut]))

(deftest scratch
  (testing "override enter and exit with override if all three are defined in ctx"
    (let [ctx (sut/context)
          ns  :test
          screen (atom "")]
      (sut/on-both ctx ns (fn [enter-sel exit-sel] (swap! screen str "a123b")))
      (sut/on-enter    ctx ns nil (fn [sel] (swap! screen str "a")))
      (sut/on-exit     ctx ns nil (fn [sel] (swap! screen str "b")))

      (sut/play ctx)
      (is (= "a123b" @screen))))

  (testing "play enter and exit if override can not be played"
    (let [ctx (sut/context)
          ns  :test
          screen (atom "")]

      (sut/on-both ctx ns (fn [enter-sel exit-sel] (swap! screen str "hello" )))
      (sut/on-exit ctx ns nil (fn [sel] (swap! screen str  "b") ))

      (sut/play ctx)

      (is (= "b" @screen)))))
