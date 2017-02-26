(ns food-closure.mathem
  (:require [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as string]
            [clj-http.client :as client]
            [food-closure.store :as store]))

(defn get-name
  [data]
  (data "ProductName"))

(defn get-image-url
  [data]
  (data "ImageUrl"))

(defn transform-to-items
  [articles]
  (map
   (fn [a] {:title      (get-name a)
            :image_link (str "https:" (get-image-url a))})
   articles))

(defrecord MatHem [search-url]
  store/Store
  (search [store get-data-fn text]
    (-> (str search-url text)
        (get-data-fn)
        (transform-to-items)
        )))

(def Mathem (MatHem.
             "https://www.mathem.se/WebServices/ProductService.asmx/SearchAndAddResult?searchText="
             ))


(defn fetch-data [search-url]
  (-> search-url
      (client/get)
      (:body)
      (json/read-str)))
