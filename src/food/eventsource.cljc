(ns food.eventsource
  (:require [food.types :as t]
            [food.macros :as m]))

(defn flatall [& args]
  (->
   (map (fn [d] [d]) args)
   (flatten)))

(defmulti fold (fn [acc d] (m/get-type d)))
(defmethod fold :CreateList [acc d]
  (let [list-name (t/CreateList-title d)]
    (assoc acc list-name (t/List list-name []))))
(defmethod fold :AddItem [acc d]
  (let [path (flatall (t/AddItem-list-name d) (t/List-items--path))
        item (t/AddItem-item d)]
    (update-in acc
               path
               conj
               item)))

(defn leave-trail
  [fun acc value]
  (conj acc (fun (last acc) value)))

(defn trail-reduce
  ([fun initial vec]
   (reduce (partial leave-trail fun)
           [initial]
           vec))
  ([initial vec]
   (trail-reduce fold initial vec)))
