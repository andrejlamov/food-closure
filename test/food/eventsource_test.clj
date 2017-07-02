(ns food.eventsource-test
  (:require [food.eventsource :as sut]
            [food.types :refer :all]
            [food.macros :refer :all]
            [clojure.test :as t]))

(def events [(CreateList "my list")

             (AddItem
              "my list"
              (Item "apple" "apple.jpg"))

             (AddItem
              "my list"
              (Item "orange" "orange.jpg"))])

(def states
  [{}
   {"my list" (List "my list" [])}
   {"my list" (List "my list" [(Item "apple" "apple.jpg")])}
   {"my list" (List "my list" [(Item "apple" "apple.jpg")
                               (Item "orange" "orange.jpg")])}])

(def states-2
  [{}
   {"my list" (List "my list" [])}
   {"my list" (List "my list" [(Item "apple" "apple.jpg")])}
   {"my list" (List "my list" [(Item "apple" "apple.jpg")
                               (Item "orange" "orange.jpg")])}
   {"my list" (List "my list" [(Item "apple" "apple.jpg")
                               (Item "orange" "orange.jpg")
                               (Item "avocado" "avocado.jpg")])}])

(t/deftest trail-reduce-test
  (t/is (= [0 1 2 3 5]
           (sut/trail-reduce + 0  [1 1 1 2]))))

(t/deftest fold
  (t/is (= {"my list" (List "my list" [])} (sut/fold {} (CreateList "my list" [])))))

(t/deftest reduce-with-types
  (t/is (= states
           (sut/trail-reduce {} events))))

(t/deftest append-new-state
  (let [event (AddItem "my list" (Item "avocado" "avocado.jpg"))]
    (t/is (= (sut/reduce-append states event)
             states-2))))
