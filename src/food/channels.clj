(ns food.channels
  (:require
   [org.httpkit.server :refer [send! Channel]]))

(defonce hub (atom #{}))

(defn subscribe [channel]
  (swap! hub conj channel))

(defn unsubscribe [channel]
  (swap! hub
         (fn [state e] (set (remove #{e} state)))
         channel))

(defn send [channel data]
  (when (satisfies? Channel channel)
    (send! channel (pr-str data)))
  {:channel channel :msg data})

(defn publish-to-all [data]
  (map #(send % data) @hub))

