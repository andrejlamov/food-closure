(ns food.mathem
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [food.eval :as e]
            [food.types :refer :all]
            [food.util :as u]))

(defn build-url [search-text]
  (let [url "https://www.mathem.se/WebServices/ProductService.asmx/SearchAndAddResult?searchText="]
    (str url search-text)))

(defn get-title
  [data]
  (data "ProductName"))

(defn get-image-url
  [data]
  (data "ImageUrl"))

(defn prepend-https [url]
  (str "https:" url))

(defn parse [text] (json/read-str text))

(defn transform [data]
  (let [items (map
               (fn [d] (let [title (get-title d)
                            image (-> d
                                      (get-image-url)
                                      (prepend-https))]
                        (Item title image)))
               data)]
    (CandidateList items)
    ))

(defn parse-and-transform [fetched-data]
  (-> fetched-data
      (parse)
      (transform)))

(defn search [search-text]
  (->> search-text
       (build-url)
       (u/get-data :http)
       (parse-and-transform)))

(defmethod e/searchQuery :Mathem [d]
  (search (SearchQuery-text d)))
