(ns food.timeline)

(defn context []
  (atom {:animations {}
         :selections {}
         }))

(defn clear [ctx]
  (swap! ctx assoc :animations {}))


(defn flatten-nodes [selection]
  #?(:cljs
     (let [acc (atom [(.. selection node)])]
       (.. selection
           (selectAll "*")
           (each #(this-as this
                    (swap! acc conj this))))
       @acc)))

(defn not-active? [selection]
  #?(:cljs
     (do
       (if (nil? selection)
         true
         (every? nil? (map (fn [t] (.. js/d3 (active t))) (flatten-nodes selection)))))))


(defn- store-node [ctx path sel]
  (swap! ctx assoc-in (concat [:selections] path) sel))

(defn- get-node [ctx path]
  (get-in @ctx (concat [:selections] path)))

(defn lookup [ctx ns f]
  (if (fn? f)
    f
    (get-in ctx [:animations ns f] (fn [& args]))))

(defn- add
  ([ctx ns t cb]
   (swap! ctx assoc-in [:animations ns t] cb))
  ([ctx ns t sel cb]
    (store-node ctx [ns t] sel)
    (swap! ctx assoc-in [:animations ns t] cb)))

(defn add-enter [ctx ns sel cb]
  (add ctx ns :enter sel cb))

(defn add-exit [ctx ns sel cb]
  (add ctx ns :exit sel cb))

(defn add-override [ctx ns cb]
  (add ctx ns :override cb))

(defn play
  ([ctx]
   (doseq [[ns _] (:animations @ctx)]
     (do
       (play ctx ns)))
   @ctx)
  ([ctx ns]
   (let [override (get-in @ctx [:animations ns :override])
         enter    (get-in @ctx [:animations ns :enter])
         exit     (get-in @ctx [:animations ns :exit])
         enter-selection (get-node ctx [ns :enter])
         exit-selection  (get-node ctx [ns :exit])
         ]
     (if (and override enter exit
              (not-active? enter-selection)
              (not-active? exit-selection))
       (override (get-node ctx [ns :enter]) (get-node ctx [ns :exit]))
       (do
         (when (and enter (not-active? enter-selection))
           (enter enter-selection))
         (when (and exit (not-active? exit-selection))
           (exit exit-selection)))
       ))))
