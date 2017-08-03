(ns food.db-test
  (:require [food.db :as sut]
            [clojure.test :refer :all]))

(deftest create-logs
  (let [{:keys [logs] :as db} (sut/construct-db)]
    (testing "empty logs on creation"
      (is (empty @logs)))
    (testing "create emtpy log"
      (is (= {"empty" []} (sut/create db "empty"))))
    (testing "create a new log with some content"
      (is (= {"empty" []
              "a new log" [1 2 3]} (sut/create db "a new log" [1 2 3]))))
    (testing "do not overwrite created log"
      (is (= {"empty" []
              "a new log" [1 2 3]} (sut/create db "a new log" [4 5 6]))))))

(deftest append-to-logs
  (let [{:keys [logs] :as db} (sut/construct-db)]
    (sut/create db "hello log")
    (is (= {"hello log" [1]} (sut/append db "hello log" 1)))
    (is (= {"hello log" [1 2]} (sut/append db "hello log" 2)))
    (sut/create db "hello second log")))

