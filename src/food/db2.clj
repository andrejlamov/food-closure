(ns food.db2
  (:require [clojure.data :refer [diff]]))

(defn construct-db
  ([logs] {:logs (atom logs)})
  ([] {:logs (atom {})}))

(defn create
  ([db name] (create db name []))
  ([{:keys [logs]} name events]
   (swap! logs update-in [name] (fn [old]
                                  (if old old events)))))
(defn append [{:keys [logs]} log event]
  (swap! logs update-in [log] #(conj % event)))

(defn io-watcher [root-path key atom old-state new-state]
  (let [[_ new-things _] (diff old-state new-state)]
    (doall (for [[log-name events] (into [] new-things)]
             (let [new-events (filter (comp not nil?) events)
                   log-path (str root-path "/" log-name)]
               (println log-path)
               ;; (spit log-path "" :append true)
               (doall (for [event (filter (comp not nil?) events)]
                        ;; (spit log-path (str (pr-str event) "\n") :append true)
                        (println event))))))))
(defn add-io-watcher [{:keys [logs]} root-path]
  (add-watch logs :write-to-disk (partial #'io-watcher root-path)))
