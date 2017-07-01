(ns food.macros)

(defmacro -defn-constructor [name & keys]
  `(defn ~name [& values#]
     (do
       (let [keyvals#  (zipmap ~(into [] keys) (into [] values#))
             meta#     {:type (keyword (quote ~name))}]
         {:data keyvals#
          :meta meta#}))))

(defmacro -defn-getter [typename key]
  `(do
     (defn ~(symbol
             (str typename "-" (name key)))
       [data#]
       (-> data# :data ~key))))

(defmacro -defn-key-path [typename key]
  `(defn ~(symbol
          (str typename "-" (name key) "--path"))
    []
     [:data ~(keyword key)]))

;; ???:
;; Function name contains the generated gensym ref
;; when looping in the macro and not using recursion
(defmacro -defn-getters
  ([name] nil)
  ([name k & keys]
   `(do (-defn-getter  ~name ~k)
        (-defn-key-path ~name ~k)
        (-defn-getters ~name ~@keys)))
  ([name k]
   `(do (-defn-getter ~name ~k)
        (-defn-key-path ~name ~k))))

(defmacro defn-type [name & keys]
  `(do
     (-defn-constructor ~name ~@keys)
     (-defn-getters ~name ~@keys)))

(defn get-type [d] (-> d :meta :type))


