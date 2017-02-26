(ns food-closure.lush-test
  (:require  [clojure.test :refer :all]
             [food-closure.store :as store]
             [food-closure.util :as util]
             [food-closure.lush :refer :all]))

(defn test-fetch-data
  [_text]
  (util/slurp-html-file "resources/test/lush-avocado-search-result.html"))

(deftest parse-search-result
  []
  (let [
        items   (store/search Lush test-fetch-data "avocado")
        avocado (first items)
        ]
    (is (= 2 (count items)))
    (is (= "Avocado Co-Wash" (:title avocado)))
    (is (= "https://res.cloudinary.com/lush/image/upload/s--AZ-8KygP--/c_fill,h_340,q_jpegmini,w_340/v1/products/main/2015/05/Avocowash_web.jpg?itok=bX9oW_w_" (:image_link avocado)))
    ))

