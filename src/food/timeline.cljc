(ns food.timeline)

(defn context []
  (atom {}))

(defn lookup [context ns f]
  (if (fn? f)
    f
    (get-in context [ns f] (fn [& args]))))

(defn add [ctx ns name timeline]
  (swap! ctx assoc-in [ns name] timeline))

(defn player [context ns timeline]
  (let [funs     (flatten (map (partial lookup context ns) timeline))
        reversed (reverse (concat funs [(fn [])]))]
    ((reduce (fn [b a] (partial a (partial b))) reversed))))

(defn play
  ([ctx]
   (doseq [[ns _] ctx]
     (play ctx ns)))
  ([ctx ns]
   (let [enter-exit (get-in ctx [ns :enter-exit])
         enter (get-in ctx [ns :enter])
         exit (get-in ctx [ns :exit])]
     (if (and enter-exit enter exit)
       (player ctx ns enter-exit)
       (do
         (player ctx ns (or enter []))
         (player ctx ns (or exit [])))))))

