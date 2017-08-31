(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.render :as r :refer [render transform]]
            [food.timeline :as tl]
            [food.macros :refer [d3]]))

(enable-console-print!)

(def anim-ctx (tl/context))
(def selections (atom {}))

(def state (atom {:top-items #{
                               "play"
                               "stop"
                               "apple"
                               "amazon"
                               }
                  :list-items #{"chrome"
                                "firefox"
                                "safari"
                                "edge"
                                "opera"}}))

(defn pos [e]
  (let [o (.. (js/$ e) offset)]
    [(.-top o) (.-left o)]))

(declare root main)

(defn save-selection [path sel]
  (swap! selections assoc-in path sel))


(defn not-active? [sel]
  (if (nil? sel)
    false
    (nil? (.. js/d3 (active sel)))))

(defn style [sel]
  (let [
        lp (js/parseInt (.. sel (style "padding-left")))
        rp (js/parseInt (.. sel (style "padding-right")))
        w  (+ lp rp (js/parseInt (.. sel (style "width"))))
        ]
    {:padding-left lp :padding-right rp :width w}))


(defn top-bar-item-exit [parent cb]
  (.. parent
      (select "i")
      (transition)
      (duration 2000)
      (style "opacity" 0)
      (on "end"
          #(.. parent
               (transition)
               (duration 2000)
               (style "width" "0px")
               (style "padding-left" "0px")
               (style "padding-right" "0px")
               (remove)
               (on "end" cb)))))

(defn top-bar-item-enter [parent cb]
  (let [{:keys [padding-left padding-right width]} (style parent)]
    (.. parent
        (style "width" 0)
        (style "padding-left" 0)
        (style "padding-right" 0)
        (transition)
        (duration 500)
        (style "width" (str width "px"))
        (style "padding-left" (str padding-left "px"))
        (style "padding-right" (str padding-right "px"))
        (on "end" (fn []
                    (.. parent
                        (select "i")
                        (transition)
                        (duration 500)
                        (style "opacity" 1)
                        (on "end" cb)))))))

(defn top-bar-item-flying [parent ns cb]
  (let [{:keys [padding-left padding-right width]} (style parent)]
    (.. parent
        (style "padding-left" 0)
        (style "padding-right" 0)
        (style "width" 0)
        transition
        (duration 500)
        (style "width" (str width "px"))
        (style "padding-left" (str padding-left "px"))
        (style "padding-right" (str padding-right "px"))
        (on "end"
            #(let [
                   e (get-in @selections [ns :exit-selection])
                   i1 (.. parent (select "i"))
                   i0 (.. js/d3 (select e) (select "i"))
                   [t1 l1] (pos (.. i1 node))
                   [t0 l0] (pos (.. i0 node))
                   t (- (- t1 t0))
                   l (- (- l1 l0 4))]
               (.. i1
                   (style "transform" (str "translate(" l "px," t "px)"))
                   (style "z-index" 1)
                   (style "opacity" 1)
                   (transition)
                   (duration 500)
                   (style "transform" (str "translate(0,0"))
                   (on "end" (fn [] (.. js/d3
                                    (select e)
                                    (transition)
                                    (duration 500)
                                    (style "height" "0")
                                    (style "padding-top" "0")
                                    (style "padding-bottom" "0")
                                    remove)
                               (cb))))
               (.. i0 (style "visibility" "hidden")))))))

(defn bottom-item-exit [parent cb]
  (.. parent
      (select "i")
      (style "opacity" 0)
      (transition)
      (on "end" (fn []
                  (.. parent
                      (style "transform" "scaleX(1)")
                      (transition)
                      (duration 500)
                      (transition)
                      (style "height" "0")
                      (style "transform" "scaleX(0)")
                      (style "padding-top" "0")
                      (style "padding-bottom" "0")
                      remove
                      (on "end" cb))))))

(defn top-bar []
  [:div.ui.top.attached.menu {:join (d3 (style "height" "6em"))}
   (for [n (:top-items @state)]
     [:div.ui.icon.item
      {:id n
       :click (fn [d]
                (swap! state update-in [:top-items] #(set (remove #{n} %)))
                (main))
       :exit (d3 (each (fn []
                         (this-as this
                           (let [self (.. js/d3 (select this))
                                 i    (.. self (select "i") node)
                                 ns   (keyword "dock" n)]
                             (when (not-active? i)
                               (tl/add anim-ctx ns :exit [(partial top-bar-item-exit self)])))))))
       :enter (d3 (each (fn []
                          (this-as this
                            (let [self       (.. js/d3 (select this))
                                  ns         (keyword "flying" n)]
                              (save-selection [ns :enter-selection] this)
                              (when (not-active? this)
                                (tl/add anim-ctx ns :enter-exit [(partial top-bar-item-flying self ns)]))
                              (when (not-active? this)
                                (tl/add anim-ctx ns :enter [(partial top-bar-item-enter self)])))))))
       }
      [:i.huge.icon {:id n
                     :join (d3 (classed n true)
                               (style "color" "red"))
                     :enter (d3 (style "opacity" 0))}]])])

(defn bottom []
  [:div.ui.bottom.attached.segment
   {:join (d3 (style "height" "100%"))}

   (for [n (:list-items @state)]
     [:div.ui.vertical.segment
      {:id n
       :enter (d3 (style "opacity" 0)
                  (transition)
                  (style "opacity" 1)
                  (attr "class" "ui vertical segment"))
       :exit  (d3 (each (fn [] (this-as this
                                (println "exit" n)
                                (let [self (.. js/d3 (select this))
                                      ns   (keyword "flying" n)
                                      flying-enter-selection (get-in @selections [ns :enter-selection])]
                                  (save-selection [ns :exit-selection] this)
                                  (when (not-active? flying-enter-selection)
                                    (tl/add anim-ctx ns :exit [(partial bottom-item-exit self)])))
                                ))))
       }
      [:div.ui.icon.item>i.huge.icon
       {
        :click (fn [d]
                 (swap! state update-in [:list-items] #(set (remove #{n} %)))
                 (swap! state update-in [:top-items] conj n)
                 (println @state)
                 (main))
        :join (d3
               (classed n true))}]])])
(defn root []
  [:div.ui.container
   (top-bar)
   (bottom)])

(defn main []
  (reset! anim-ctx {})
  (render
   (.. js/d3 (select "#app"))
   (root))
  (tl/play @anim-ctx))

(main)

