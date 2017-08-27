(ns food.timeline)

(defn context []
  (atom {}))

(defn lookup [context ns f]
  (if (fn? f)
    f
    (get-in context [ns f] identity)))

(defn add [ctx ns name timeline]
  (swap! ctx assoc-in [ns name] timeline))

(defn dummy-player [context ns timeline]
  (doseq [f (flatten (map (partial lookup context ns) timeline))]
    (f)))

(defn play
  ([player ctx]
   (doseq [[ns _] ctx]
     (play player ctx ns)))
  ([player ctx ns]
   (let [enter-exit (get-in ctx [ns :enter-exit])
         enter (get-in ctx [ns :enter])
         exit (get-in ctx [ns :exit])]
     (if (and enter-exit enter exit)
       (player ctx ns enter-exit)
       (do
         (player ctx ns (or enter []))
         (player ctx ns (or exit [])))))))


