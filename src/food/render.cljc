(ns food.render
  (:require
   #?(:cljs [cljsjs.d3])
   [food.macros :refer [d3]]
   [clojure.string :as string]
   #?(:clj [clojure.core.match :refer [match]]
      :cljs [cljs.core.match :refer-macros [match]])))

(defn flatten-until-children [children]
  (let [recurse
        (if (= '(()) children)
                  true
                  (let [[[head & _] & other] children]
                    (sequential? head)))

        ]
    (if recurse
      (flatten-until-children (first children))
      children)))

(defn parse-tag [head]
  (map (fn [e] [(keyword e) {}]) (string/split (subs (str head) 1) #"\>")))

(defn nest [head props children]
  (let [tags0 (parse-tag head)]
    (loop [tags (into [] (reverse
                          tags0))]
      (match tags
             [child [parent props] & other] (recur
                                       (into [] (concat [[parent props child]] other)))
             [it] it))))

(defn transform [thing]
  (let [[head props children]
        (match thing
          [(head :guard keyword?) (props    :guard map?)         (children :guard sequential?)] thing
          [(head :guard keyword?) (children :guard sequential?)]                                [head {} [children]]
          [(head :guard keyword?) (props    :guard map?)         & children]                    [head props children]
          [(head :guard keyword?) & children]                                                   [head {} children])
        ]
    [head props (map transform (flatten-until-children children))]))

(defn destruct2 [[d a b]]
  (let [[tag & classes] (string/split (str d) #"\.")
        classattr (string/join " " classes)
        [props children] (match [a b]
                           [(children :guard sequential?) nil] [{} children]
                           [(props :guard map?) (children :guard sequential?)] [props children])
        props (merge {:merge classattr} props)]

    [(subs tag 1) props (map destruct2
                             (flatten-until-children children))]))

(defn destruct [[tag funs & children]]
  (let [res [tag funs (or children [])]]
    #?(:clj res
       :cljs (js->clj res :keywordize-keys true))))

(defn children-in-collection? [children]
  (if (= '(()) children)
    true
    (let [[[head & _] :as child & other] children]
      (vector? head))))

(defn render [parent children]
  #?(:clj [parent children]
     :cljs
     (if (children-in-collection? children)
       (render parent (first children))
       (let [joined  (.. parent
                         (selectall #(this-as this
                                              (let [nodes (js/array.from (aget this "childnodes"))]
                                                (.filter nodes (fn [n] (.-tagname n))))))
                         (data (clj->js children) (fn [d i]
                                                    (let [[tag {:keys [id]} children] (destruct d)]
                                                      (or id i)))))
             exited  (.. joined exit)

             entered  (.. joined
                          enter
                          (append
                           (fn [d i]
                             (.createelement
                              js/document
                              (let [[tag & _] (destruct d)] tag)))))]
         (.. exited
             (each (fn [d]
                     (this-as this
                              (let [self (.. js/d3 (select this))
                                    [tag {:keys [onexit]} children] (destruct d)
                                    onexit (or onexit (d3 remove))]
                                (.. (onexit self)
                                    (on "end" (fn []
                                                (this-as this
                                                         (.. js/d3
                                                             (select this)
                                                             (remove)))))))))))
         (.. entered
             (each (fn [d i]
                     (this-as this
                              (let [[tag {:keys [merge enter onenter]} children] (destruct d)
                             ;; todo: enter should have prio over merge
                                    draw   (or merge enter identity)
                                    onenter (or onenter identity)
                                    self (->> (.. js/d3 (select this))
                                              draw
                                              onenter)]
                                (render self children))))))
         (.. joined
             (each (fn [d]
                     (let [[tag {:keys [merge]} children] (destruct d)
                           draw  (or merge identity)]
                       (this-as this
                                (->> (.. js/d3 (select this))
                                     (draw)
                                     (#(render %1 children))))))))))))

