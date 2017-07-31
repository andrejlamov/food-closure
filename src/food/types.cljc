(ns food.types
  (:require [food.macros :refer [defn-type]]))

(defn-type Subscribe)
(defn-type Unsubscribe)

(defn-type Mathem)

(defn-type SearchQuery :text :store)
(defn-type AllLists)

(defn-type CandidateList :items)
(defn-type Item :title :image)
(defn-type List :name :items)
(defn-type Lists :lists)
(defn-type CreateList :name)
(defn-type AddItem :list-name :item)
