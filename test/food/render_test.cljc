(ns food.render-test
  (:require [food.render :as sut]
            [food.macros :refer [d3]]
            [clojure.test :refer :all]))

(deftest d3-composition
  (is (= {:merge (d3 (attr "div" "ui container")
                     (style "color" "red"))}
         {:merge (concat (d3 (attr "div" "ui container"))
                         (d3 (style "color" "red")))}

         (sut/merge-props {:merge (d3 (attr "div" "ui container"))}
                          {:merge (d3 (style "color" "red"))})))

  (is (= {}
         (sut/merge-props nil nil))))

(deftest head-test

  (is (= ["div" {:merge (d3 (attr "class" "a b c"))}]
         (sut/build-element "div.a.b.c")))

  (is (= [["div" {}] ["a" {:merge (d3 (attr "class" "b c d")
                                      (style "color" "red"))} []]]
         (sut/destruct-head :div>a.b.c.d {:merge (d3 (style "color" "red"))} [])))

  (is (= ["div" {} [["div" {} [["a" {} [["i" {:merge (d3 (attr "class" "c d")
                                                         (style "color" "blue"))} []]]]]]]]
         (sut/nest (sut/destruct-head :div>div>a>i.c.d {:merge (d3 (style "color" "blue"))} []))))

  (is (= ["div" {:enter identity} []]
         (sut/nest (sut/destruct-head :div {:enter identity} [])))))

(deftest transform-test
  (is (= ["div" {:merge (d3 (attr "class" "a b c"))} []]
         (sut/transform [:div.a.b.c])))
  (is (=
       ["d" {} [["a" {:merge (d3 (attr "class" "1 2"))}
                 [["b" {:merge (d3 (attr "class" "1 2 3"))} []]
                  ["c" {} []]]]]]

       (sut/transform [:d>a.1.2
                       [[:b.1.2.3]
                        [:c]]])

       (sut/transform [:d>a.1.2
                       [:b.1.2.3]
                       [:c]])))

  (is (=
       ["div" {} [["a" {:merge (d3 (attr "class" "1 2")
                                   (style "color" "red"))}
                   [["h" {:merge (d3 (attr "class" "1 2 3")
                                     (style "color" "green"))} []]]]]]
       (sut/transform [:div>a.1.2
                       {:merge (d3 (style "color" "red"))}
                       [:h.1.2.3
                        {:merge (d3 (style "color" "green"))}]]))))

