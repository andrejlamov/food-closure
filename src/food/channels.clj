(ns food.channels
  (:require
   [org.httpkit.server :refer :all]))

(defn subscribe [channel channels]
  (when channel
    (swap! channels conj channel)))

(defn unsubscribe [channel channels]
  (when channel
    (swap! channels
           (fn [state e] (set (remove #{e} state)))
           channel)))

(defn publish [channels data]
  (doseq [c channels]
    (when c
      (send! c (pr-str data))))
  data)

