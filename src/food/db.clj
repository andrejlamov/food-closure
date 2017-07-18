(ns food.db
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.edn :as edn]))


(defn path [db-root list-name]
  (str db-root "/" list-name))

(defn create-event-log [path]
  (io/make-parents path))

(defn append-to-event-log [path d]
  (create-event-log path)
  (spit path (str (pr-str d) "\n") :append true))

(defn read-event-log [path]
  (->> path
       (slurp)
       (string/split-lines)
       (map edn/read-string)))

(defn reduce-events [events]
  events)

(defn read-all-logs [db-root]
  (->> (io/file db-root)
       (file-seq)
       (rest)
       (map read-event-log)))

