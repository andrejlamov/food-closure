(ns food-closure.karamellkungen
  (:require
   [net.cgrand.enlive-html :as html]
   [clojure.string :as string]
   [clj-http.client :as client]
   [food-closure.util :as util]
   [food-closure.store :as store]))


(defn get-html-articles
  [html]
  (html/select html [:div.product-list :article]))

(defn get-name
  [html]
  (-> (html/select html [:h1.product-title html/text-node])
      (string/join)))

(defn get-image-link
  [html]
  (-> (html/select html [:a.product-image :img])
      (first)
      (#(html/attr-values %1 :src))
      (string/join)))

(defn transform-to-items
  [articles]
  (map (fn [a] {:title      (get-name a)
                :image_link (get-image-link a)
                }) articles))

(defrecord KaramellKungen [search-url]
  store/Store
  (search [store get-data-fn text]
    (-> (str search-url text)
        (get-data-fn)
        (get-html-articles)
        (transform-to-items))))

(def Karamellkungen (KaramellKungen. "http://karamellkungen.se/?s="))

(defn fetch-data [search-url]
   (-> search-url
       (client/get)
       (:body)
       (util/from-string-to-html)))

