(ns food.systembolaget-test
  (:require
   [food.systembolaget :as sut]
   [food.types :as t]
   [clojure.test :as test]
   [food.util :as util]
   ))
(defn not-nil? [expr]
  (not (nil? expr)))

(defn valid-item? [item]
  (every? string?
          [(t/Item-title item)
           (t/Item-image item)]))

(test/deftest build-url-test
  (test/is (= "https://www.systembolaget.se/api/productsearch/search/sok-dryck/?sortdirection=Ascending&site=all&fullassortment=0&searchquery=renat"
           (sut/build-url "renat"))))

(test/deftest json-parse-test
  (let [result (->> "resources/test/systembolaget-renat-search-result.json"
                    (util/get-data :file)
                    (sut/parse-and-transform))
        items (t/CandidateList-items result)]
    (test/is (every? valid-item? items))
    (test/is (> (count items) 0))))
