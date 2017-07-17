(ns food.eval
  (:require [food.util :as u]
            [org.httpkit.server :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [food.macros :refer :all ]
            [food.types :refer :all]))

(defmulti searchQuery (fn [d] (->> d (SearchQuery-store) (get-type))))

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

(defn create-db [db-root list-name]
  (let [path (str db-root "/" list-name)]
    (io/make-parents (str db-root "/" list-name))
    path))

(defn append-to-db [db-root list-name d]
  (let [path (create-db db-root list-name)]
    (spit path (str (pr-str d) "\n") :append true)))

(defn read-from-db [db-root list-name]
  (->> (str db-root "/" list-name)
       (slurp)
       (string/split-lines)
       (map edn/read-string)))

(defmulti evaluate (fn [d s] (get-type d)))
(defmethod evaluate :Subscribe   [_ s]
  (subscribe (Scope-channel s)
             (Scope-channel-hub s)))
(defmethod evaluate :Unsubscribe [_ s]
  (unsubscribe (Scope-channel s)
               (Scope-channel-hub s)))
(defmethod evaluate :SearchQuery [d s]
  (publish [(Scope-channel s)] (searchQuery d)))
(defmethod evaluate :CreateList [d s]
  (create-db (Scope-db-root s) (CreateList-name d))
  (publish (Scope-channel-hub s) d))
(defmethod evaluate :AddItem [d s]
  (append-to-db (Scope-db-root s)
                (AddItem-list-name d)
                (AddItem-item d))
  (publish (Scope-channel-hub s) d))

