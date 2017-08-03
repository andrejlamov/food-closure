(ns food.eval
  (:require
   [food.hub :as hub]
   [food.db2 :as db]))

(defonce hub (hub/construct-channel-hub))
(defonce db (db/construct-db))

(hub/add-io-watcher hub)
(db/add-io-watcher db "db")

(defmulti searchQuery (fn [client-state]
                        (get-in client-state [:search :store])))
(defn evaluate
  ([msg channel]
   (evaluate msg channel hub db))
  ([msg channel hub db]
   (let [{:keys [operation client-state]} msg]
     (case operation
       :Unsubscribe (do (hub/send hub channel [
                                               [[:channel-hub :connected] false]
                                               ])
                        (hub/unsubscribe hub channel))
       :Subscribe (do (hub/subscribe hub channel)
                      (hub/send hub channel
                                [
                                 [[:channel-hub :connected] true]
                                 ]))
       :Search (let [msg [[[:search-result :list] (searchQuery client-state)]]]
                 (hub/send hub channel msg))
       )
     )))
