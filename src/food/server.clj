(ns food.server
  (:gen-class)
  (:require [org.httpkit.server :refer :all]
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]
            [compojure.route :refer [resources]]
            [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET]]
            [clojure.tools.namespace.repl :refer [disable-unload!]]
            [food.handlers]))

(disable-unload!)

(defonce server (atom nil))

(defn handle [channel data]
  (food.handlers/on-receive-handler channel data))

(defn ws-handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (println "*** chanel closed: " status)))
    (on-receive channel (fn [data] (handle channel data)))))

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
