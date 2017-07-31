(ns food.eval
  (:require
   [food.db :as db]
   [food.channels :as channels]
   [food.macros :refer :all]
   [food.types :refer :all]))

(defmulti searchQuery (fn [d] (->> d (SearchQuery-store) (get-type))))

(defmulti evaluate (fn [d channel db-root] (get-type d)))
(defmethod evaluate :Subscribe   [_ channel db-root]
  (channels/subscribe channel))
(defmethod evaluate :Unsubscribe [_ channel db-root]
  (channels/unsubscribe channel))
(defmethod evaluate :SearchQuery [d channel _]
  (channels/send channel (searchQuery d)))
(defmethod evaluate :CreateList [d channel db-root]
  (db/create-event-log db-root (CreateList-name d))
  (channels/publish-to-all d))
(defmethod evaluate :AddItem [d channel db-root]
  (db/append-to-event-log db-root (AddItem-list-name d) d)
  (evaluate (AllLists) channel db-root))
(defmethod evaluate :AllLists [_ channel db-root]
  (->> db-root
       (db/read-all-logs)
       (channels/send channel)))
