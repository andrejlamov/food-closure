(ns food.eventsource)

(defn leave-trail
  [fun acc value]
  (conj acc (fun (last acc) value)))

(defn trail-reduce [fun initial vec]
  (reduce (partial leave-trail fun)
          [initial]
          vec))
