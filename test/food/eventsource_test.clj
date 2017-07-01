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

(def reduced-events
  [{}
   {"my list" (List "my list" [])}
   {"my list" (List "my list" [(Item "apple" "apple.jpg")])}
   {"my list" (List "my list" [(Item "apple" "apple.jpg")
                               (Item "orange" "orange.jpg")])}])

(def reduced-events-appended-avocado
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

(t/deftest reduce-with-types
  (t/is (= reduced-events
           (sut/trail-reduce {} events))))

(t/deftest append-to-trail-list
  (let [event (AddItem "my list" (Item "avocado" "avocado.jpg"))]
    (t/is (= (sut/trail-reduce-event reduced-events event)
             reduced-events-appended-avocado
           ))
    ))


(t/deftest flatten-everything
  (t/is (= [1 2 3]
           (sut/flatall 1 [2] [[[3]]]))))
