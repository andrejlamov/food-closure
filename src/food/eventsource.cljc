(ns food.eventsource
  (:require [food.types :as t]
            [food.macros :as m]))

(defmulti fold (fn [acc d] (m/get-type d)))
(defmethod fold :CreateList [acc d]
  (let [list-name (t/CreateList-title d)]
    (assoc acc list-name (t/List list-name []))))
(defmethod fold :AddItem [acc d]
  (let [path (into [(t/AddItem-list-name d)] (t/List-items--path))
        item (t/AddItem-item d)]
    (update-in acc
               path
               conj
               item)))

(defn leave-trail
  [fun acc value]
  (conj acc (fun (last acc) value)))

(defn trail-reduce
  ([fun initial lst]
   (reduce (partial leave-trail fun)
           [initial]
           lst))
  ([initial lst]
   (trail-reduce fold initial lst)))

(defn reduce-append-state [states event]
  (let [last-state (last states)]
    (->> (trail-reduce last-state [event])
         (last)
         (conj states))))

(defn reduce-append-swap [states event]
  (swap! states reduce-append-state event))

