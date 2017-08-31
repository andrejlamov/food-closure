(ns food.timeline-test
  (:require  [clojure.test :refer :all]
             [food.timeline :as sut]))

(deftest scratch
  (testing "override enter and exit with override if all three are defined in ctx"
    (let [ctx (sut/context)
          ns  :test
          screen (atom "")]
      (sut/add-override ctx ns (fn [enter-sel exit-sel] (swap! screen str "a123b")))
      (sut/add-enter    ctx ns nil (fn [sel] (swap! screen str "a")))
      (sut/add-exit     ctx ns nil (fn [sel] (swap! screen str "b")))

      (sut/play ctx)
      (is (= "a123b" @screen))))

  (testing "play enter and exit if override can not be played"
    (let [ctx (sut/context)
          ns  :test
          screen (atom "")]

      (sut/add-override ctx ns (fn [enter-sel exit-sel] (swap! screen str "hello" )))
      (sut/add-exit ctx ns nil (fn [sel] (swap! screen str  "b") ))

      (sut/play ctx)

      (is (= "b" @screen)))))
