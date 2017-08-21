(ns food.macros)

(defn js-to-clj [d]
  #?(:clj d
     :cljs (js->clj d :keywordize-keys true)))


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
       (->> data#
            js-to-clj
            :data
            ~key))))

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

(defmacro eval-d3 [[a b c]]
  `(list '~a ~b ~c))

(defmacro eval-d3-list
  ([a]
   `(list (eval-d3 ~a)))
  ([a & args]
   `(cons (eval-d3 ~a) (eval-d3-list ~@args))))

;; cljs-env and if-cljs from https://github.com/plumatic/schema/blob/master/src/clj/schema/macros.clj#L10-L19
(defn cljs-env?
  "Take the &env from a macro, and tell whether we are expanding into cljs."
  [env]
  (boolean (:ns env)))

(defmacro if-cljs
  "Return then if we are generating cljs code and else for Clojure code.

  https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ"
  [then else]
  (if (cljs-env? &env) then else))

(defmacro d3 [& expr]
  `(if-cljs
    (fn [p#] (.. p# ~@expr))
    (eval-d3-list  ~@expr)))
