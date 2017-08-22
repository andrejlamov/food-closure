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
            (sequential? head)))]
    (if recurse
      (flatten-until-children (first children))
      children)))

(defn build-element [tag]
  (let [[t & classes]  (string/split tag #"\.")
        class          (string/join " " classes)]
    (if (empty? class)
      [t {}]
      [t {:merge (d3 (attr "class" class))}])))

(defn merge-whatever [a b]
  (match [a b]
         [(a :guard fn?)   (b :guard fn?)]   (comp (or a identity) (or b identity))
         [(a :guard coll?) (b :guard coll?)] (filter (comp not nil?) (concat a b))
         [& _] nil))

(defn merge-props [props-a props-b]
  (let [a (:merge props-a)
        b (:merge props-b)
        merged (merge-whatever a b)]
      (merge {} props-a props-b {:merge merged})))

(defn destruct-head [head props children]
  (let [[tag & tags] (reverse (string/split (name head) #"\>"))
        tags'        (map (fn [t] (build-element t)) tags)
        [tag' props'] (build-element tag)
        tag''         [tag' (merge-props props' props) children]]
    (reverse (cons tag'' tags'))))

(defn nest [tags]
  (loop [tags' (into [] (reverse
                         tags))]
    (match tags'
      [child [parent props] & other] (recur
                                      (into [] (concat [[parent props [child]]] other)))
      [it] it)))

(defn transform [thing]
  (let [[head props children]
        (match thing
          [(head :guard keyword?) (props    :guard map?)         (children :guard sequential?)] thing
          [(head :guard keyword?) (children :guard sequential?)]                                [head {} [children]]
          [(head :guard keyword?) (props    :guard map?)         & children]                    [head props children]
          [(head :guard keyword?) & children]                                                   [head {} children])
        children' (map transform (flatten-until-children children))]
    (nest (destruct-head head props children'))))

(defn render [parent children]
  #?(:clj [parent children]
     :cljs
     (let [joined  (.. parent
                       (selectAll #(this-as this
                                            (let [nodes (js/Array.from (aget this "childNodes"))]
                                              (.filter nodes (fn [n] (.-tagName n))))))
                       (data (clj->js children) (fn [d i]
                                                  (let [[tag {:keys [id]} children] (js->clj d)]
                                                    (or id (str tag "_" i))))))
           exited  (.. joined exit)

           entered  (.. joined
                        enter
                        (append
                         (fn [d i]
                           (.createElement
                            js/document
                            (let [[tag & _] d] tag)))))]
       (.. exited
           (each (fn [d]
                   (this-as this
                            (let [self                            (.. js/d3 (select this))
                                  [tag {:keys [onexit]} children] d
                                  onexit                          (or onexit (d3 remove))]
                              (.. (onexit self)
                                  (on "end" (fn []
                                              (this-as this
                                                       (.. js/d3
                                                           (select this)
                                                           (remove)))))))))))
       (.. entered
           (each (fn [d i]
                   (this-as this
                     (let [[tag {:keys [merge enter onenter]} children] (js->clj d :keywordize-keys true)
                           ;; TODO: enter should have prio over merge
                           draw   (or merge enter identity)
                           onenter (or onenter identity)
                           self (->> (.. js/d3 (select this))
                                     draw
                                     onenter)]
                       (render self children))))))
       (.. joined
           (each (fn [d]
                   (let [[tag {:keys [merge]} children] (js->clj d :keywordize-keys true)
                         draw  (or merge identity)]
                     (this-as this
                              (->> (.. js/d3 (select this))
                                   (draw)
                                   (#(render %1 children)))))))))))

