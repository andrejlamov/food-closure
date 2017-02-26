(ns food-closure.mathem-test
  (:require [food-closure.mathem :refer :all]
            [clojure.data.json :as json]
            [clojure.test :refer :all]
            [clojure.data.json :as json]
            [food-closure.store :as store]))

(defn test-fetch-data
  [_text]
  (-> (slurp "resources/test/mathem-lingongrova-search-result.json")
      (json/read-str)))

(deftest item-test
  (testing "Get item from search result"
    (let [
          item (-> (store/search Mathem test-fetch-data "lingongrova")
                   (first)
                   )
          ]
      (is (= "Lingongrova 500g Pågen"
             (:title item)))
      (is (= "https://static.mathem.se/shared/images/products/small/07311071330525_c1l1.jpg"
             (:image_link item)))
      )))
