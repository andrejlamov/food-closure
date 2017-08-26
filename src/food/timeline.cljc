(ns food.timeline)

(defn timelines []
  (atom {}))

(defn lookup [timelines f]
  (if (string? f)
    (get timelines f)
    f))

(defn run-timeline [timeline timelines]
  (doall (map (fn [f] (f)) (flatten (map (partial lookup timelines) timeline)))))

(defn run [timelines]
  (if (contains? timelines "enter-exit")
    (let [line (get timelines "enter-exit")]
      (run-timeline line timelines))
    (do
      (run-timeline (get timelines "enter" []) timelines)
      (run-timeline (get timelines "exit" []) timelines))))

