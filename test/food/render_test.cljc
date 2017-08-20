(ns food.render-test
  (:require [food.render :as sut]
            [food.macros :refer [d3]]
            [clojure.test :refer :all]))

(deftest parse-tag
  (is (= [[:div {}] [:a {:enter identity} [:h1]]]

         (sut/destruct-head :div>a {:enter identity} [:h1])))

  (is (= [:div {} [
                   [:div {} [
                             [:a {} [
                                     [:i {} [
                                             [:h1]]]]]]]]]

         (sut/nest (sut/destruct-head :div>div>a>i {} [[:h1]]))))

  (is (= [:div {:enter identity} []]
         (sut/nest (sut/destruct-head :div {:enter identity} []))))
  )

(deftest transform
  (let [onenter (d3 (attr "style" "test"))]
    (is (=
         [:d {} [
                 [:a {} [
                         [:b {:onenter onenter} []]
                         [:c {} []]
                         ]
                  ]
                 ]
          ]
         (sut/transform [:d>a
                         [[:b {:onenter onenter}]
                          [:c ]]
                         ])
         (sut/transform [:d>a
                         [:b {:onenter onenter}]
                         [:c {}]])
         ))))
