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
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]))

(disable-unload!)

(defonce server (atom nil))

(defn evaluate [data channel]
  (food.util/log data)
  (food.eval/evaluate data channel))

(defn ws-handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (evaluate (food.types/Unsubscribe) channel)))
    (on-receive channel (fn [data] (evaluate (edn/read-string data) channel)))))

(defroutes routes
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/")))

(def route-handler
  (site #'routes))

(defn start-server []
  (reset! server (run-server route-handler {:port 8080})))

(defn restart-server []
  (if-not (nil? @server)
    (do (@server :timeout 100)
        (reset! server nil)
        (start-server))
    (start-server)))
