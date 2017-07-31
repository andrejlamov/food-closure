(ns food.eval
  (:require
   [org.httpkit.server :refer :all]
   [food.db :as db]
   [food.channels :as channels]
   [food.macros :refer :all]
   [food.types :refer :all]))

(defmulti searchQuery (fn [d] (->> d (SearchQuery-store) (get-type))))

(defmulti evaluate (fn [d s] (get-type d)))
(defmethod evaluate :Subscribe   [_ s]
  (channels/subscribe (Scope-channel s)))
(defmethod evaluate :Unsubscribe [_ s]
  (channels/unsubscribe (Scope-channel s)))
(defmethod evaluate :SearchQuery [d s]
  (channels/publish-to-all (searchQuery d)))
(defmethod evaluate :CreateList [d s]
  (db/create-event-log
   (db/path (Scope-db-root s) (CreateList-name d)))
  (channels/publish-to-all d))
(defmethod evaluate :AddItem [d s]
  (db/append-to-event-log
   (Scope-db-root s) (AddItem-list-name d)
   d)
  (evaluate (AllLists) s))
(defmethod evaluate :AllLists [_ s]
  (->> (Scope-db-root s)
       (db/read-all-logs)
       (channels/send (Scope-channel s))))

