(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.render :as r :refer [render transform]]
            [food.animation :as animation])
  (:require-macros
   [food.macros :refer [d3]]))

(enable-console-print!)

(def ctx (animation/context))

(defonce state (atom {:top-items #{
                               "play"
                               "stop"
                               "apple"
                               "amazon"
                               }
                  :list-items #{"chrome"
                                "firefox"
                                "google"
                                "edge"
                                "opera"}}))

(defn pos [e]
  (let [o (.. (js/$ e) offset)]
    [(.-top o) (.-left o)]))

(declare root main)

(defn style [sel]
  (let [
        lp (js/parseInt (.. sel (style "padding-left")))
        rp (js/parseInt (.. sel (style "padding-right")))
        w  (+ lp rp (js/parseInt (.. sel (style "width"))))
        ]
    {:padding-left lp :padding-right rp :width w}))


(defn top-bar-item-exit [parent]
  (.. parent
      (select "i")
      (transition)
      (duration 500)
      (style "opacity" 0)
      (on "end"
          #(.. parent
               (transition)
               (duration 500)
               (style "width" "0px")
               (style "padding-left" "0px")
               (style "padding-right" "0px")
               (remove)))))

(defn top-bar-item-enter [parent]
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
                        ))))))

(defn flying [parent ns enter-selection exit-selection]
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
        (on "end" (fn []
                    (let [
                          i1 (.. parent (select "i"))
                          i0 (.. exit-selection (select "i"))
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
                          (on "end" (fn [] (..
                                           exit-selection
                                           (transition)
                                           (duration 500)
                                           (style "height" "0")
                                           (style "padding-top" "0")
                                           (style "padding-bottom" "0")
                                           remove)
                                      )))
                      (.. i0 (style "visibility" "hidden"))))))))

(defn bottom-item-exit [parent]
  (.. parent
      (select "i")
      (transition)
      (style "opacity" 0)
      (on "end" (fn []
                  (.. parent
                      (style "transform" "scaleX(1)")
                      (transition)
                      (style "height" "0")
                      (style "transform" "scaleX(0)")
                      (style "padding-top" "0")
                      (style "padding-bottom" "0")
                      remove
                      )))))

(defn top-bar []
  [:div.ui.top.attached.menu {:join (d3 (style "height" "6em"))}
   (for [n (:top-items @state)]
     [:div.ui.icon.item
      {:id n
       :click (fn [d]
                (swap! state update-in [:top-items] #(set (remove #{n} %)))
                (main))
       :exit (fn [selection] (animation/on-exit ctx (keyword "dock" n) selection top-bar-item-exit))
       :enter (fn [selection] (let [ns (keyword "flying" n)]
                               (animation/on-enter ctx ns selection top-bar-item-enter)
                               (animation/on-both ctx ns (partial flying selection ns))))
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
       :enter #(.. % (style "opacity" 0)
                   (transition)
                   (style "opacity" 1)
                   (attr "class" "ui vertical segment"))
       :exit (fn [selection] (animation/on-exit ctx (keyword "flying" n) selection bottom-item-exit))
       }
       [:div.ui.icon.item>i.huge.icon
        {
         :click (fn [d]
                  (swap! state update-in [:list-items] #(set (remove #{n} %)))
                  (swap! state update-in [:top-items] conj n)
                  (println @state)
                  (main))
         :join #(.. % (classed n true))
         }
        ]
      ])])

(defn root []
  [:div.ui.container
   (top-bar)
   (bottom)])

(defn main []
  (animation/clear ctx)
  (render
   (.. js/d3 (select "#app"))
   (root))
  (animation/play ctx))

(main)
