(ns food.timeline)

(defn context []
  (atom {:animations {}
         :selections {}
         }))

(defn store-node [ctx path sel]
  (swap! ctx assoc-in (concat [:selections] path) sel))

(defn get-node [ctx path]
  (get-in @ctx (concat [:selections] path)))


(defn clear [ctx]
  (swap! ctx assoc :animations {}))

(defn lookup [ctx ns f]
  (if (fn? f)
    f
    (get-in ctx [:animations ns f] (fn [& args]))))

(defn- add [ctx ns t timeline]
  (if (fn? timeline)
    (swap! ctx assoc-in [:animations ns t] [timeline])
    (swap! ctx assoc-in [:animations ns t] timeline)))

(defn add-enter [ctx ns timeline]
  (add ctx ns :enter timeline))

(defn add-exit [ctx ns timeline]
  (add ctx ns :exit timeline))

(defn add-override [ctx ns timeline]
  (add ctx ns :override timeline))

(defn player [ctx ns timeline]
  (let [funs     (flatten (map (partial lookup ctx ns) timeline))
        reversed (reverse (concat funs [(fn [])]))]
    ((reduce (fn [b a] (partial a (partial b))) reversed))))

(defn play
  ([ctx]
   (doseq [[ns _] (:animations ctx)]
     (play ctx ns))
   ctx)
  ([ctx ns]
   (let [override (get-in ctx [:animations ns :override])
         enter    (get-in ctx [:animations ns :enter])
         exit     (get-in ctx [:animations ns :exit])]
     (if (and override enter exit)
       (player ctx ns override)
       (do
         (player ctx ns (or enter []))
         (player ctx ns (or exit [])))))))

