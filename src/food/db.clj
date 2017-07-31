(ns food.db
  (:require
   [clojure.java.io :as io]
   [food.types :refer :all]
   [clojure.string :as string]
   [clojure.edn :as edn]))

(defn- path [db-root list-name]
  (str db-root "/" list-name))

(defn create-event-log [db-root list-name]
  (io/make-parents
   (path db-root list-name)))

(defn append-to-event-log [db-root list-name d]
  (create-event-log db-root list-name)
  (spit (path db-root list-name) (str (pr-str d) "\n") :append true))

(defn read-event-log [file]
  (->> file
       (slurp)
       (string/split-lines)
       (map edn/read-string)
       (List (.getName file))))

(defn reduce-events [events]
  events)

(defn read-all-logs [db-root]
  (->> (io/file db-root)
       (file-seq)
       (rest)
       (map read-event-log)
       (Lists)))
