(ns food.systemtest
  (:require
   [food.mathem :as mathem]))

(defn fetch-from-mathem []
  (let [res (mathem/search "lingongrova")]
    res))
