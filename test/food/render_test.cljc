(ns food.render-test
  (:require [food.render :as sut]
            [food.macros :refer [d3]]
            [clojure.test :refer :all]))

(deftest tag
  ;; (is (=
  ;;      ["div" {:merge "a b c"} []]
  ;;      (sut/destruct2 [:div.a.b.c])))
  ;; (is (=

  ;;      ["div" {:merge "c d e"} []]
  ;;      (sut/destruct2 [:div.a.b.c {:merge "c d e"}])
  ;;        ))
  (let [onenter (d3 (attr "style" "test"))]
    (is (=
         [:a {} [
                 [:b {:onenter onenter} []]
                 [:c {} []]
                 ]
          ]
         (sut/transform [:a
                         [[:b {:onenter onenter}]
                          [:c]]])
         (sut/transform [:a
                         [:b {:onenter onenter}]
                         [:c {}]])))))
