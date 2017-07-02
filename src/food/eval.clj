(ns food.eval
  (:require [food.util :as u]
            [org.httpkit.server :refer :all]
            [food.util :as u]
            [food.eventsource :as e]
            [food.macros :as m]
            [food.types :as t]))

(defmulti searchQuery (fn [d] (->> d (t/SearchQuery-store) (m/get-type))))

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
      (u/log c)
      (send! c (pr-str data))))
  data)

(defmulti evaluate (fn [d _] (m/get-type d)))
(defmethod evaluate :Subscribe   [_ {:keys [channel channel-hub]}]
  (subscribe channel channel-hub))
(defmethod evaluate :Unsubscribe [_ {:keys [channel channel-hub]}]
  (unsubscribe channel channel-hub))
(defmethod evaluate :SearchQuery [d {:keys [channel]}]
  (publish [channel] (searchQuery d)))
(defmethod evaluate :default     [d {:keys [data-states channel-hub]}]
  (e/reduce-append-swap data-states d)
  (publish channel-hub d))
