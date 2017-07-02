(ns food.eventsource
  (:require [food.types :as t]
            [food.macros :as m]))

(defmulti fold (fn [_ d] (m/get-type d)))
(defmethod fold :CreateList [state d]
  (let [list-name (t/CreateList-title d)]
    (assoc state list-name (t/List list-name []))))
(defmethod fold :AddItem [state d]
  (let [path (into [(t/AddItem-list-name d)] (t/List-items--path))
        item (t/AddItem-item d)]
    (update-in state
               path
               conj
               item)))

(defn leave-trail
  [fun state value]
  (conj state (fun (last state) value)))

(defn trail-reduce
  ([fun initial lst]
   (reduce (partial leave-trail fun)
           [initial]
           lst))
  ([initial lst]
   (trail-reduce fold initial lst)))

(defn reduce-append [states event]
  (let [last-state (last states)]
    (->> (trail-reduce last-state [event])
         (last)
         (conj states))))

(defn reduce-append-swap [states event]
  (swap! states reduce-append event))

