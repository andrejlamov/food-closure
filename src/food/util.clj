(ns food.util
  (:require [clj-http.client :as client]
            [clojure.core.match :refer [match]]
            [food.globals :as g]))

(defn get-data [method url]
  (match method
    :http (:body (client/get url))
    :file (slurp url)
    :else nil))

(defn log [d]
  (println "*** server" d)
  d)

(defn scope [ws-channel channel-hub states]
  {:channel ws-channel
   :channel-hub channel-hub
   :data-states states})

