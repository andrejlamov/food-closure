(ns food.render-test
  (:require [food.render :as sut]
            [food.macros :refer [d3]]
            [clojure.test :refer :all]))

(deftest head

  (is (= ["div" {:merge (d3 (attr "class" "a b c"))}]
         (sut/build-element "div.a.b.c")))

  (is (= [["div" {}] ["a" {:enter (d3 (style "a" "b"))
                           :merge (d3 (attr "class" "b c d"))} []]]
         (sut/destruct-head :div>a.b.c.d {:enter (d3 (style "a" "b"))} [])))

  (is (= ["div" {} [["div" {} [["a" {} [["i" {:enter identity} []]]]]]]]
         (sut/nest (sut/destruct-head :div>div>a>i {:enter identity} []))))

  (is (= ["div" {:enter identity} []]
         (sut/nest (sut/destruct-head :div {:enter identity} [])))))

(deftest transform
  (is (= ["div" {:merge (d3 (attr "class" "a b c"))} []]
         (sut/transform [:div.a.b.c])))
  (is (=
       ["d" {} [["a" {:merge (d3 (attr "class" "1 2"))} [["b" {:merge (d3 (attr "class" "1 2 3"))} []]
                                                         ["c" {} []]]]]]

       (sut/transform [:d>a.1.2
                       [[:b.1.2.3]
                        [:c]]])

       (sut/transform [:d>a.1.2
                       [:b.1.2.3]
                       [:c]]))))
