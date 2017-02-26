(ns food-closure.lush
  (:require
   [net.cgrand.enlive-html :as html]
   [clojure.string :as string]
   [food-closure.util :as util]
   [clj-http.client :as client]
   [food-closure.store :as store]))

(defn get-html-articles
  [html]
  (html/select html [:#main-content :.search-results-wrapper :.object-product-module]))

(defn get-name
  [html]
  (-> (html/select html [:div.product-module-title :a.handwritten html/text])
      (string/join)
      (string/trim)))

(defn get-image-link
  [html]
  (-> (html/select html [:.product-module-product-image :img])
      (first)
      (#(html/attr-values %1 :src))
      (string/join)
      ))

(defn transform-to-items
  [articles]
  (map (fn [a] {:title (get-name a)
                :image_link (get-image-link a)
                })
       articles))

(defrecord LushLtd [search-url]
  store/Store
  (search [store get-data-fn text]
    (-> (str search-url text)
        (get-data-fn)
        (get-html-articles)
        (transform-to-items)
        )))

(def Lush (LushLtd. "https://se.lush.com/search/site/"))

(defn fetch-data [search-url]
  (-> search-url
      (client/get)
      (:body)
      (util/from-string-to-html)))

