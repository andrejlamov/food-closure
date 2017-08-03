(ns food.hub
  (:require
   [org.httpkit.server :refer [send! Channel]]))

(defn construct-channel-hub
  ([] {:channels (atom #{})
       :output-queue (atom [])
       :output-limit 1})
  ([props] (merge (construct-channel-hub) props)))

(defn subscribe [{:keys [channels] :as hub} channel]
  (swap! channels conj channel))

(defn unsubscribe [{:keys [channels] :as hub} channel]
  (swap! channels
         (fn [state e] (set (remove #{e} state)))
         channel))

(defn send [{:keys [output-queue output-limit]} channel msg]
  (let [msg {:channel channel :msg msg}]
    (swap! output-queue (fn [queue msg] (take-last output-limit (concat queue [msg]))) msg)))

(defn publish [{:keys [channels output-queue] :as hub} msg]
  (map #(send hub % msg) @channels))

(defn io-watcher [_key _atom _old-msgs new-msgs]
  (let [{:keys [channel data] :as msg} (last new-msgs)]
    (println "*** sending" msg)
    (let [data (update-in msg [:channel] str)]
      (println msg)
      (send! channel (pr-str data)))))

(defn add-io-watcher [{:keys [output-queue]}]
  (add-watch output-queue :websocket-send #'io-watcher))
