(ns food.server
  (:gen-class)
  (:require [clojure.edn :as edn]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            [clojure.tools.namespace.repl :refer [disable-unload!]]
            [food.eval]
            [food.macros]
            [org.httpkit.server :refer :all]
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]))


(disable-unload!)

(defn evaluate [channel msg]
  (food.eval/evaluate channel msg))

(defn ws-handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (evaluate {:operation :Unsubscribe} channel)))
    (on-receive channel (fn [data] (evaluate (edn/read-string data) channel)))))

(defroutes routes
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/")))

(def route-handler
  (site #'routes))
