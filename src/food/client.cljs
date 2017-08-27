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

(defn if-not-active
  ([this path fun]
   (when (not (or
               (.. js/d3 (active (.. (get-in @selections path) node)))
               (.. js/d3 (active this))))
     (fun)))
  ([this fun]
  (when (not (.. js/d3 (active this)))
    (fun))))

(defn top-bar []
  [:div.ui.top.attached.menu {:join (d3 (style "height" "6em"))}
   (for [n (:top-items @state)]
     [:div.ui.icon.item
      {:id n
       :click (fn [d]
                (swap! state update-in [:top-items] (partial remove #{n}))
                (main))
       :exit (d3 (each (fn []
                         (this-as this
                                  (println "exit" n)
                                  (let [self (.. js/d3 (select this))
                                        w     (.. self (style "width"))
                                        lp     (.. self (style "padding-left"))
                                        rp     (.. self (style "padding-right"))
                                        f (fn [cb]
                                            (.. self
                                                (select "i")
                                                (transition 500)
                                                (style "opacity" 0)
                                                (on "end" (fn []
                                                            (.. self
                                                                (transition)
                                                                (duration 5000)
                                                                (style "width" "0px")
                                                                (style "padding-left" "0px")
                                                                (style "padding-right" "0px")
                                                                (remove)
                                                                (on "end" cb))))))]
                                    (if-not-active this #(tl/add anim-ctx (keyword "dock" n) :exit [f])))))))

       :enter (d3 (each (fn []
                          (this-as this


                            (let [self (.. js/d3 (select this))
                                  lp (js/parseInt (.. self (style "padding-left")))
                                  rp (js/parseInt (.. self (style "padding-right")))
                                  w  (+ lp rp (*  2 (js/parseInt (.. self (style "width")))))
                                  enter  (fn [cb]
                                           (.. self
                                               (style "width" 0)
                                               (style "padding-left" 0)
                                               (style "padding-right" 0)
                                               (transition)
                                               (duration 500)
                                               (style "width" (str w "px"))
                                               (style "padding-left" (str lp "px"))
                                               (style "padding-right" (str rp "px"))
                                               (on "end" (fn []
                                                           (.. self
                                                               (select "i")
                                                               (transition)
                                                               (duration 500)
                                                               (style "opacity" 1)
                                                               (on "end" cb))))))
                                  enter-exit (fn [cb]
                                               (.. self
                                                   (style "padding-left" 0)
                                                   (style "padding-right" 0)
                                                   (style "width" 0)
                                                   transition
                                                   (duration 1500)
                                                   (style "width" "6em")
                                                   (style "padding-left" lp)
                                                   (style "padding-right" rp)
                                                   (on "end"
                                                       (fn []
                                                         (let [
                                                               e        (get-in @anim-ctx [(keyword "flying" n) :exit-selection])
                                                               i1       (.. self (select "i"))
                                                               i0       (.. e (select "i"))
                                                               [t1 l1] (pos (.. i1 node))
                                                               [t0 l0] (pos (.. i0 node))
                                                               t (- (- t1 t0))
                                                               l (- (- l1 l0 4))]
                                                           (.. i1
                                                               (style "transform" (str "translate(" l "px," t "px)"))
                                                               (style "z-index" 1)
                                                               (style "opacity" 1)
                                                               (transition)
                                                               (duration 5000)
                                                               (style "transform" (str "translate(0,0"))
                                                               (on "end" (fn [] (.. e
                                                                                   (transition)
                                                                                   (duration 5000)
                                                                                   (style "height" "0")
                                                                                   (style "padding-top" "0")
                                                                                   (style "padding-bottom" "0")
                                                                                   remove))))
                                                           (.. i0 (style "visibility" "hidden")

                                                               ))))))]

                              (swap! selections assoc-in [(keyword "flying" n) :enter-selection] self)
                              (if-not-active this #(tl/add anim-ctx (keyword "flying" n) :enter-exit [enter-exit]))
                              (if-not-active this #(tl/add anim-ctx (keyword "flying" n) :enter [enter])))))))

       }
      [:i.huge.icon {:id n
                     :join (d3 (classed n true)
                               (style "color" "red"))
                     :enter (d3 (style "opacity" 0))}]])])

(defn item-list []
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
                                        (let [self (.. js/d3 (select this))
                                              exit (fn [cb]
                                                     (.. self
                                                      (select "i")
                                                      (transition)
                                                      (duration 500)
                                                      (style "opacity" 0)
                                                      (on "end" (fn []
                                                                  (.. self
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
                                              ]
                                          (if-not-active this [(keyword "flying" n) :enter-selection] #(tl/add anim-ctx (keyword "flying" n) :exit [exit]))
                                          (if-not-active this #(tl/add anim-ctx (keyword "flying" n) :exit-selection self))
                                          )))))
       }
      [:div.ui.icon.item>i.huge.icon
       {
        :click (fn [d]
                 (swap! state update-in [:list-items] (partial remove #{n}))
                 (swap! state update-in [:top-items] conj n)
                 (main))
        :join (d3
               (classed n true))}]])])
(defn root []
  [:div.ui.container
   (top-bar)
   (item-list)])

(defn main []
  (reset! anim-ctx {})
  (render
   (.. js/d3 (select "#app"))
   (root))
  (tl/play @anim-ctx))

(main)

