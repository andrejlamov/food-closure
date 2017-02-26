(ns food-closure.system-test
  (:require
   [food-closure.store :as store]
   [food-closure.database :as db]
   [food-closure.mathem :as mathem]
   [food-closure.database :as db]
   [food-closure.karamellkungen :as karamell]))

(def conf (db/conf *ns*))

(defn compose-a-list
  []
  (db/test-setup conf)
  (let [
        ;; Create list
        list-id     (db/create-list conf "sunday list")

        ;; Fetch items from Mathem and add to list
        special     (->
                     (store/search mathem/Mathem mathem/fetch-data "lingongrova special")
                     (first)) ;; first match
        naturell    (->
                     (store/search mathem/Mathem mathem/fetch-data "yoghurt naturell")
                     (#(nth %1 2))) ;; third match
        special-id    (db/add-item-to-list conf list-id special)
        naturell-id      (db/add-item-to-list conf list-id naturell)

        ;; Fetch some candy from Karamellkungen and add to list
        ferrari     (-> (store/search karamell/Karamellkungen karamell/fetch-data "ferrari")
                        (first))
        ferrari-id-1 (db/add-item-to-list conf list-id ferrari)
        ferrari-id-2 (db/add-item-to-list conf list-id ferrari)

        ;; Asssert size of list
        items       (db/get-all-items conf list-id)
        _           (assert (= 4 (count items)))
        ]
    (db/test-teardown conf)
    items
    )
  )

(compose-a-list)
