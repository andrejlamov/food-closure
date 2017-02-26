(ns food-closure.karamellkungen-test
  (:require [food-closure.karamellkungen :refer :all]
            [clojure.test :refer :all]
            [food-closure.store :as store]
            [net.cgrand.enlive-html :as html]
            [food-closure.util :as util]))


(defn test-fetch-data
  [_text]
  (util/slurp-html-file "resources/test/karamellkungen-banan-search-result.html"))

(deftest html-banan-list-test
  (testing "Get item from search result"
    (let [
          item (-> (store/search Karamellkungen test-fetch-data "banan")
                   (first))
          ]
      (is (= "Banana Skids Mini"
              (:title item)))
      (is (= "http://candyking.com/ckse/wp-content/uploads/sites/2/2017/02/banana-skids-mini_picture-560x560.png"
              (:image_link item)))
      )))
