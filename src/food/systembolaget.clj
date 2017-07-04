(ns food.systembolaget
  (:require
   [clojure.data.json :as json]
   [food.types :as t]
   ))


(defn build-url [search-text]
  (let [url "https://www.systembolaget.se/api/productsearch/search/sok-dryck/?sortdirection=Ascending&site=all&fullassortment=0&searchquery="]
    (str url search-text)))

(defn prepend-https [url]
  (str "https:" url))

(defn transform [data]
  (->> data
       (map (fn [d] (t/Item
                     (d "ProductNameBold")
                     (prepend-https (get-in d ["Thumbnail" "ImageUrl"])))))
       t/CandidateList))

(defn parse [data]
  (-> (json/read-str data)
      (get "ProductSearchResults")))

(defn parse-and-transform [fetched-data]
  (-> fetched-data
      (parse)
      (transform)))
