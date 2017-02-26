(ns food-closure.database-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as sql]
            [food-closure.database :refer :all])
  )

(def conf (db *ns*))

(defn fixture [f]
  (test-setup conf)
  (f)
  (test-teardown conf)
  )

(use-fixtures :each fixture)

(deftest create-lists-tests
  (testing "Add  to lists"
    (let [
          ;; Items
          eggs       {:title "Eggs", :image_link "eggs.jpg" :store "ICA"}
          grova      {:title "Lingongrova" :image_link "grova.jpg" :store "ICA"}
          popcorn    {:title "Popcorn" :image_link "popcorn.jpg" :store "ICA"}
          ;; Two lists
          brunch-id  (create-list conf "Sunday brunch")
          snacks-id  (create-list conf "Snacks")
          ;; Add to lists
          eggs-id    (add-item-to-list conf brunch-id eggs)
          grova-id   (add-item-to-list conf brunch-id grova)
          popcorn-id (add-item-to-list conf snacks-id popcorn)
          ;; Get items from one of the lists
          items      (get-all-items conf brunch-id)
          ]
      (is (= 1 brunch-id))
      (is (= 2 snacks-id))
      (is (= 1 eggs-id))
      (is (= 2 grova-id))
      (is (= [(merge {:id eggs-id} eggs)
              (merge {:id grova-id} grova)] items)
      ))))
