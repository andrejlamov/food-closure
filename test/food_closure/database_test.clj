(ns food-closure.database-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as sql]
            [food-closure.database :refer :all])
  )

(def test-conf (conf *ns*))

(defn fixture [f]
  (test-setup test-conf)
  (f)
  (test-teardown test-conf)
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
          brunch-id  (create-list test-conf "Sunday brunch")
          snacks-id  (create-list test-conf "Snacks")
          ;; Add to lists
          eggs-id    (add-item-to-list test-conf brunch-id eggs)
          grova-id   (add-item-to-list test-conf brunch-id grova)
          popcorn-id (add-item-to-list test-conf snacks-id popcorn)
          ;; Get items from one of the lists
          items      (get-all-items test-conf brunch-id)
          ]
      (is (= 1 brunch-id))
      (is (= 2 snacks-id))
      (is (= 1 eggs-id))
      (is (= 2 grova-id))
      (is (= [(merge {:id eggs-id} eggs)
              (merge {:id grova-id} grova)] items)
      ))))
