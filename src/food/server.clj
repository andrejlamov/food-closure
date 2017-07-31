(ns food.server
  (:gen-class)
  (:require [clojure.edn :as edn]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            food.eval
            food.types
            food.util
            [org.httpkit.server :refer :all]
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]))

(defonce channel-hub (atom #{}))
(def db-path "db/")

(defn evaluate [data channel]
  (food.util/log data)
  (food.eval/evaluate
   data
   (food.types/Scope channel channel-hub db-path)))

(defn ws-handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (evaluate (food.types/Unsubscribe) channel)))
    (on-receive channel (fn [data] (evaluate (edn/read-string data) channel)))))

(defroutes routes
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/")))

(def route-handler
  (site #'routes))
