(ns food.mathem-test
  (:require [food.mathem :as sut]
            [clojure.test :as t]
            [clojure.string :as s]
            [food.util :as util]))

(defn not-nil? [expr]
  (not (nil? expr)))

(defn valid-item? [{:keys [image title]}]
  (every? not-nil? [image title]))

(t/deftest transform-test
  (let
      ;; Act
   [result (->> "resources/test/mathem-lingongrova-search-result.json"
                (util/get-data :file)
                (sut/parse-and-transform))]
    ;; Assert
    (t/is (every? valid-item? result))))
