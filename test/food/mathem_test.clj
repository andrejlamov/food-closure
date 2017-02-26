(ns food.mathem-test
  (:require [food.mathem :as sut]
            [clojure.test :as t]
            [food.types :as ty]
            [clojure.string :as s]
            [food.util :as util]))

(defn not-nil? [expr]
  (not (nil? expr)))

(defn valid-item? [item]
  (every? not-nil?
          [(ty/Item-title item)
           (ty/Item-image item)]))

(t/deftest transform-test
  (let
      ;; Act
   [result (->> "resources/test/mathem-lingongrova-search-result.json"
                (util/get-data :file)
                (sut/parse-and-transform))]
    ;; Assert
    (t/is (every? valid-item? (ty/CandidateList-items result)))))
