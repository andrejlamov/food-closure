(ns food.render-test
  (:require [food.render :as sut]
            [food.macros :refer [d3]]
            [clojure.test :refer :all]))

(deftest head

  (is (= [:div {:merge_class "a b c"}] (sut/build-props "div.a.b.c")))

  (is (= [[:div {}] [:a {:enter identity
                         :merge_class "b c d"} [:h1]]]

         (sut/destruct-head :div>a.b.c.d {:enter identity} [:h1])))

  (is (= [:div {} [
                   [:div {} [
                             [:a {} [
                                     [:i {:enter identity} [
                                                            [:h1]]]]]]]]]

         (sut/nest (sut/destruct-head :div>div>a>i {:enter identity} [[:h1]]))))

  (is (= [:div {:enter identity} []]
         (sut/nest (sut/destruct-head :div {:enter identity} []))))
  )

(deftest transform
  (let [onenter (d3 (attr "style" "test"))]
    (is (=
         [:d {} [
                 [:a {} [
                         [:b {:onenter onenter
                              :merge_class "1 2 3"} []]
                         [:c {} []]
                         ]
                  ]
                 ]
          ]
         (sut/transform [:d>a
                         [[:b.1.2.3 {:onenter onenter}]
                          [:c ]]
                         ])
         (sut/transform [:d>a
                         [:b.1.2.3 {:onenter onenter}]
                         [:c {}]])
         ))))
