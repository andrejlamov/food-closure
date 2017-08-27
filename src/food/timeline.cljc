(ns food.timeline)

(defn context []
  (atom {}))

(defn lookup [context ns f]
  (if (fn? f)
    f
    (get-in context [ns f])))

(defn play-timeline [context ns timeline]
  (doall (map (fn [f] (f)) (flatten (map (partial lookup context ns) timeline)))))

(defn play
  ([context]
   (doseq [[ns _] context]
     (play context ns)))
  ([context ns]
   (if (get-in context [ns :enter-exit])
     (let [timeline (get-in context [ns :enter-exit])]
       (play-timeline context ns timeline))
     (do
       (play-timeline context ns (get-in context [ns :enter] []))
       (play-timeline context ns (get-in context [ns :exit] []))))))

