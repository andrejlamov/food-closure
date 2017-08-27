(ns food.timeline)

(defn context []
  (atom {}))

(defn lookup [context ns f]
  (if (fn? f)
    f
    (get-in context [ns f])))

(defn add [ctx ns name timeline]
  (swap! ctx assoc-in [ns name] timeline))

(defn play-timeline [context ns timeline]
  (doall (map (fn [f] (f)) (flatten (map (partial lookup context ns) timeline)))))

(defn play
  ([context]
   (doseq [[ns _] context]
     (play context ns)))
  ([context ns]
   (let [enter-exit (get-in context [ns :enter-exit])
         enter (get-in context [ns :enter])
         exit (get-in context [ns :exit])]
     (if (and enter-exit enter exit)
       (play-timeline context ns enter-exit)
       (do
         (play-timeline context ns (or enter []))
         (play-timeline context ns (or exit [])))))))

