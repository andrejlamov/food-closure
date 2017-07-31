(ns food.server
  (:gen-class)
  (:require [clojure.edn :as edn]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            [food.eval :as e]
            [food.types :refer :all]
            food.util
            [org.httpkit.server :refer :all]
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]))

(def db-root "db/")

(defn ws-handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (e/evaluate (Unsubscribe) channel db-root)))
    (on-receive channel (fn [data] (e/evaluate (edn/read-string data) channel db-root)))))

(defroutes routes
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/")))

(def route-handler
  (site #'routes))
