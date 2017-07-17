(ns food.db
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.edn :as edn]))

(defn create-event-log [db-root list-name]
  (let [path (str db-root "/" list-name)]
    (io/make-parents (str db-root "/" list-name))
    path))

(defn append-to-event-log [db-root list-name d]
  (let [path (create-event-log db-root list-name)]
    (spit path (str (pr-str d) "\n") :append true)))

(defn read-from-event-log [db-root list-name]
  (->> (str db-root "/" list-name)
       (slurp)
       (string/split-lines)
       (map edn/read-string)))
