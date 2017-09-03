(ns food.animation)

(defn context []
  (atom {:animations {}
         :selections {}
         }))

(defn clear [ctx]
  (swap! ctx assoc :animations {}))


(defn- flatten-nodes [selection]
  #?(:cljs
     (let [acc (atom [(.. selection node)])]
       (.. selection
           (selectAll "*")
           (each #(this-as this
                    (swap! acc conj this))))
       @acc)))

(defn- not-active? [selection]
  #?(
     :clj true
     :cljs
     (do
       (if (nil? selection)
         true
         (every? nil? (map (fn [t] (.. js/d3 (active t))) (flatten-nodes selection)))))))


(defn- store-selection [ctx path sel]
  (swap! ctx assoc-in (concat [:selections] path [:selection]) sel))

(defn get-selection [ctx path]
  (get-in @ctx (concat [:selections] path [:selection])))

(defn set-animation-active [ctx path]
  (swap! ctx assoc-in (concat [:selections] path [:active]) true))

(defn set-animation-inactive [ctx path]
  (swap! ctx assoc-in (concat [:selections] path [:active]) false))

(defn animation-active? [ctx path]
  (get-in @ctx (concat [:selections] path [:active])))

(defn- add
  ([ctx ns t cb]
   (swap! ctx assoc-in [:animations ns t] cb))
  ([ctx ns t sel cb]
    (store-selection ctx [ns t] sel)
    (swap! ctx assoc-in [:animations ns t] cb)))

(defn on-enter [ctx ns sel cb]
  (add ctx ns :enter sel cb))

(defn on-exit [ctx ns sel cb]
  (add ctx ns :exit sel cb))

(defn on-both [ctx ns cb]
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
         enter-selection (get-selection ctx [ns :enter])
         exit-selection  (get-selection ctx [ns :exit])
         ]
     (if (and override enter exit
              )
       (override (get-selection ctx [ns :enter]) (get-selection ctx [ns :exit]))
       (do
         (when (and enter (not-active? enter-selection))
           (enter enter-selection))
         (when (and exit (not-active? exit-selection))
           (exit exit-selection)))
       ))))
