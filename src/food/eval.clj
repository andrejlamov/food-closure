(ns food.eval
  (:require [food.util :as u]
            [org.httpkit.server :refer :all]
            [food.mathem :as mathem]
            [food.util :as u]
            [food.eventsource :as e]
            [food.macros :as m]
            [food.globals :as g]
            [food.types :as t]))

(defmulti searchQuery t/SearchQuery-store)
(defmethod searchQuery :mathem [d]
  (mathem/search (t/SearchQuery-text d)))

(defn subscribe [channel channels]
  (swap! channels conj channel))

(defn unsubscribe [channel channels]
  (when channel
    (swap! channels
           (fn [state e] (set (remove #{e} state)))
           channel)))

(defn publish [channel data]
  (when channel
    (send! channel (pr-str data))))

(defmulti evaluate (fn [d _] (m/get-type d)))
(defmethod evaluate :Subscribe   [_ scope] (subscribe   (:channel scope) (:channel-hub scope)))
(defmethod evaluate :Unsubscribe [_ scope] (unsubscribe (:channel scope) (:channel-hub scope)))
(defmethod evaluate :SearchQuery [d scope]
  (u/log (count @g/channel-hub))
  (let [res (searchQuery d)]
    (publish (:channel scope) res)
    res))
(defmethod evaluate :default [d scope]
  (let [states (:data-states scope)]
    (e/reduce-append-swap states d)))
