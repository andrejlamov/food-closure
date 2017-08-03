(ns food.server
  (:gen-class)
  (:require [clojure.edn :as edn]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            [food.eval :as e]
            [food.hub :as hub]
            [food.db2 :as db]
            [org.httpkit.server :refer :all]
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]))

(defonce hub (hub/construct-channel-hub))
(defonce db (db/construct-db))

(hub/add-io-watcher hub)
(db/add-io-watcher db "db")

(defn ws-handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (e/evaluate {:operation :Unsubscribe} channel hub db)))
    (on-receive channel (fn [data] (e/evaluate (edn/read-string data) channel hub db)))))

(defroutes routes
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/")))

(def route-handler
  (site #'routes))
