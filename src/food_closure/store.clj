(ns food-closure.store)

(defprotocol Store
  (search [store fetch-data-fn text]))

