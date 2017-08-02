(ns food.hub)

(defn construct-channel-hub []
  {:channels (atom #{})})

(defn subscribe [{:keys [channels] :as hub} channel]
  (swap! channels conj channel))

(defn send [channel msg]
  {:channel channel :msg msg})

(defn publish [{:keys [channels] :as hub} msg]
  (map #(send % msg) @channels))

