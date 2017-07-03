(ns food.systembolaget-test
  (:require [food.systembolaget :as sut]
            [clojure.test :as t]))

(t/deftest build-url-test
  (t/is (= "https://www.systembolaget.se/api/productsearch/search/sok-dryck/?searchquery=renat&sortdirection=Ascending&site=all&fullassortment=0"
           (sut/build-url "renat"))))
