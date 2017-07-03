(ns food.systembolaget)


(defn build-url [search-text]
  (let [url "https://www.systembolaget.se/api/productsearch/search/sok-dryck/?sortdirection=Ascending&site=all&fullassortment=0&searchquery="]
    (str url search-text)))
