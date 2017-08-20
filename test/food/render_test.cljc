(ns food.render-test
  (:require [food.render :as sut]
            [food.macros :refer [d3]]
            [clojure.test :refer :all]))

(deftest parse-tag
  (is (= [:div {} [:div {} [:a {} [:i {}]]]] (sut/nest :div>div>a>i {} [])))
  (is (= [:div {}] (sut/nest :div {} []))))

(deftest transform
  (let [onenter (d3 (attr "style" "test"))]
    (is (=
         [:d {} [
                 [:a {} [
                         [:b {:onenter onenter} []]
                         [:c {} []]]]]]
         (sut/transform [:d [:a
                             [[:b {:onenter onenter}]
                              [:c]]]])
         (sut/transform [:d [:a
                             [:b {:onenter onenter}]
                             [:c {}]]])
         ))))
