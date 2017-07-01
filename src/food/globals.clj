(ns food.globals
  (:require
   [clojure.tools.namespace.repl :refer [disable-unload!]]))

(disable-unload!)

(defonce channel-hub (atom #{}))
