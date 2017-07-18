(ns food.db-test
  (:require [food.db :as sut]
            [clojure.java.io :as io]
            [food.types :refer :all]
            [clojure.test :as t]
            [food.db :as db]))

(def db-root (str "target/" *ns* "/"))

(defn fixture [f]
  (io/make-parents (str db-root "."))
  (f)
  (doall (->> (io/file db-root)
              (file-seq)
              (reverse)
           (map (fn [f] (io/delete-file f true))))))

(t/use-fixtures :each fixture)

(t/deftest test-db
  (let [fruits [(AddItem "fruits" (Item "apple" "apple.jpg"))
                (AddItem "fruits" (Item "orange" "orange.jpg"))]
        _      (doall
                (map (partial db/append-to-event-log (str db-root "fruits")) fruits))
        read-fruits  (first (db/read-all-logs db-root))]
    (t/is (= fruits read-fruits))))

