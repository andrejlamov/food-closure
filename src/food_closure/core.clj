(ns food-closure.core
  (:gen-class)
  (:require [food-closure.system-test :as system-test]))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Let's find closure!")
  (system-test/compose-a-list)
  )
