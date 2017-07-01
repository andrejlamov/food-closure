(ns food.types
  (:require [food.macros :refer [defn-type]]))

(defn-type Subscribe)
(defn-type Unsubscribe)

(defn-type SearchQuery :text :store)
(defn-type CandidateList :items)
(defn-type Item :title :image)
(defn-type List :name :items)
(defn-type CreateList :title)
(defn-type AddItem :list-name :item)
