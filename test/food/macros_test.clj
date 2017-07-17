(ns food.macros-test
  (:require [food.macros :as sut]
            [clojure.test :as t]))

(sut/defn-type Hello :msg :volume)

(defmulti hellomulti sut/get-type)
(defmethod hellomulti :Hello   [_] true)
(defmethod hellomulti :default [_] false)

(t/deftest constructor-and-getters
  (let [
        text   "hello!!!"
        num    10
        hello (Hello text num)
        ]
    (t/is (= text (Hello-msg hello)))
    (t/is (= num (Hello-volume hello)))
    (t/is (= [:data :msg] (Hello-msg--path)))
    (t/is (= :Hello (sut/get-type hello)))
    (t/is (hellomulti hello))
    ))
