(ns food.server
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.tools.namespace.repl :refer [disable-unload!]]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            food.eval
            food.types
            food.util
            [org.httpkit.server :refer :all]
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]
            [food.globals :as g]))

(disable-unload!)

(defonce channel-hub (atom #{}))

(defn evaluate [data channel]
  (food.util/log data)
  (food.eval/evaluate
   data
   (food.util/scope channel channel-hub nil)))

(defn ws-handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (evaluate (food.types/Unsubscribe) channel)))
    (on-receive channel (fn [data] (evaluate (edn/read-string data) channel)))))

(defroutes routes
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/")))

(def route-handler
  (site #'routes))
