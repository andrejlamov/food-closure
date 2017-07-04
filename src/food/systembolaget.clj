(ns food.systembolaget
  (:require [clojure.data.json :as json]))


(defn build-url [search-text]
  (let [url "https://www.systembolaget.se/api/productsearch/search/sok-dryck/?sortdirection=Ascending&site=all&fullassortment=0&searchquery="]
    (str url search-text)))

(defn transform [data]
  data)

(defn parse [data]
  (get (json/read-str data) "ProductSearchResults"))

(defn parse-and-transform [fetched-data]
  (-> fetched-data
      (parse)
      (transform)))
