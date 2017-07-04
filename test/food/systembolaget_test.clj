(ns food.systembolaget-test
  (:require [food.systembolaget :as sut]
            [clojure.test :as t]
            [food.util :as util]))

(t/deftest build-url-test
  (t/is (= "https://www.systembolaget.se/api/productsearch/search/sok-dryck/?sortdirection=Ascending&site=all&fullassortment=0&searchquery=renat"
           (sut/build-url "renat"))))

(t/deftest json-parse-test
  (let [result (->> "resources/test/systembolaget-renat-search-result.json"
                    (util/get-data :file)
    -transform))]
    (t/is (= #{} result))))
