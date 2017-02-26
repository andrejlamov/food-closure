(ns food.eventsource-test
  (:require [food.eventsource :as sut]
            [food.types :refer :all]
            [food.macros :refer :all]
            [clojure.test :as t]))

(t/deftest trail-reduce-test
  (t/is (= [0 1 2 3 5]
           (sut/trail-reduce + 0  [1 1 1 2]))))

(defn flatall [& args]
  (->
   (map (fn [d] [d]) args)
   (flatten)))

(defmulti fold (fn [acc d] (get-type d)))

(defmethod fold :CreateList [acc d]
  (let [list-name (CreateList-title d)]
    (assoc acc list-name (List list-name []))))

(defmethod fold :AddItem [acc d]
  (let [path (flatall (AddItem-list-name d) (List-items--path))
        item (AddItem-item d)]
    (update-in acc
               path
               conj
               item)))

(t/deftest reduce-with-types
  (let [events [(CreateList "my list")

                (AddItem
                 "my list"
                 (Item "apple" "apple.jpg"))

                (AddItem
                 "my list"
                 (Item "orange" "orange.jpg"))]]
    (t/is (=
           [{}

            {"my list" (List "my list" [])}

            {"my list" (List "my list" [(Item "apple" "apple.jpg")])}

            {"my list" (List "my list" [(Item "apple" "apple.jpg")
                                        (Item "orange" "orange.jpg")])}]

           (sut/trail-reduce fold {} events)))))

(t/deftest flatten-everything
  (t/is (= [1 2 3]
           (flatall 1 [2] [[[3]]]))))
