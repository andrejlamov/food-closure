(ns food-closure.database
  (:require [clojure.java.jdbc :as sql]))

;;(jdbc/insert! db-spec :table {:col1 42 :col2 "123"}) ;; Create
;; (jdbc/query   db-spec ["SELECT * FROM table WHERE id = ?" 13]) ;; Read
;; (jdbc/update! db-spec :table {:col1 77 :col2 "456"} ["id = ?" 13]) ;; Update
;; (jdbc/delete! db-spec :table ["id = ?" 13]) ;; Delete

(defn get-scope-id
  [scope-id-map]
  (get scope-id-map (keyword "scope_identity()")))


(defn conf
  [name]
  {:classname   "org.h2.Driver"
   :subprotocol "h2:mem"
   :subname     (str name ";DB_CLOSE_DELAY=-1")
   :user        "test"
   :password    ""})


(defn create-list
  [db title]
  (-> (sql/insert! db
                   :lists
                   {:title title})
      (first)
      (get-scope-id)))

(defn map-item-to-list
  [db list-id item-id]
  (-> (sql/insert! db
                   :item_list_maps
                   {:item_id item-id
                    :list_id list-id})
      (first)
      (get-scope-id)))

(defn create-item
  [db item]
  (-> (sql/insert! db
                   :items
                   item)
      (first)
      (get-scope-id)
      ))

(defn add-item-to-list
  [db list-id item]
  (let [
        item-id (create-item db item)
        ]
    (map-item-to-list db list-id item-id)
    item-id))

(defn remove-item-from-list
  [db list-id item-id]
  (sql/delete! db :item_list_maps ["item_id = ? AND list_id = ?" item-id list-id])
  (sql/delete! db :items ["id = ?" item-id]) )

(defn get-all-items [db list-id]
  (sql/query db ["
SELECT a.*
FROM items AS a
INNER JOIN item_list_maps AS m
WHERE a.id = m.item_id AND m.list_id = ?
" list-id]))

(defn init-items
  [db]
  (sql/execute! db (sql/create-table-ddl
                    :items [
                            [:id         "INT AUTO_INCREMENT PRIMARY KEY"]
                            [:title      "VARCHAR(255)"]
                            [:image_link "VARCHAR(255)"]
                            [:store      "VARCHAR(255)"]
                            ])))

(defn init-lists
    [db]
    (sql/execute! db (sql/create-table-ddl
                      :lists
                      [
                       [:id    "INT AUTO_INCREMENT PRIMARY KEY"]
                       [:title "VARCHAR(255) NOT NULL"]
                       ])))

(defn init-item-list-maps
  [db]
  (sql/execute! db (sql/create-table-ddl
                    :item_list_maps
                    [
                     [:item_id "INT"]
                     [:list_id "INT"]
                     ["FOREIGN KEY (item_id) REFERENCES public.items(id)"]
                     ["FOREIGN KEY (list_id) REFERENCES public.lists(id)"]
                     ])))

(defn drop-db
  [db name]
  (try
    (sql/execute! db (sql/drop-table-ddl name))
    (catch Exception e)))

(defn test-setup
  [db]
  (try
    (init-items db)
    (init-lists db)
    (init-item-list-maps db)
  (catch Exception e)))

(defn test-teardown
  [db]
  (doall (map (partial drop-db db) [:item_list_maps :items :lists])))

