(ns food.handlers
  (:require [food.util :as u]
            [org.httpkit.server :refer :all]
            [food.mathem :as mathem]
            [food.macros :as m]
            [food.types :as t]))


(defmulti searchQuery t/SearchQuery-store)
(defmethod searchQuery :mathem [d]
  (mathem/search (t/SearchQuery-text d)))

(defmulti evaluate m/get-type)
(defmethod evaluate :SearchQuery [d] (searchQuery d))

(defn read-data [d]
  (clojure.edn/read-string d))

(defn on-receive-handler [channel data]
  (->> data
       (read-data)
       (evaluate)
       (pr-str)
       (u/log)
       (send! channel)))
