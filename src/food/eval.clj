(ns food.eval
  (:require [food.util :as u]
            [org.httpkit.server :refer :all]
            [food.mathem :as mathem]
            [food.util :as u]
            [food.macros :as m]
            [food.globals :as g]
            [food.types :as t]))

(defmulti searchQuery t/SearchQuery-store)
(defmethod searchQuery :mathem [d]
  (mathem/search (t/SearchQuery-text d)))

(defn subscribe [channel channels]
  (swap! channels conj channel))

(defn unsubscribe [channel channels]
  (swap! channels
         (fn [state e] (set (remove #{e} state)))
         channel))

(defn publish [channel data]
  (send! channel (pr-str data)))

(defmulti evaluate (fn [d channel] (m/get-type d)))
(defmethod evaluate :Subscribe   [_ channel] (subscribe channel g/channel-hub))
(defmethod evaluate :Unsubscribe [_ channel] (unsubscribe channel g/channel-hub))
(defmethod evaluate :SearchQuery [d channel]
  (u/log (count @g/channel-hub))
  (->> (searchQuery d)
       (publish channel)))

