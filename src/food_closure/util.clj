(ns food-closure.util
  (:require
   [net.cgrand.enlive-html :as html]))

(defn from-string-to-html
  [text]
  (-> (java.io.StringReader. text)
      (html/html-resource)))

(defn slurp-html-file
  [file-name]
  (-> file-name
      (slurp)
      (from-string-to-html)))