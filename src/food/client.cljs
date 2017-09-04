(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [food.render :as r :refer [render transform]]
            [food.animation :as animation]))

(enable-console-print!)

(def ctx (animation/context))

(declare root main bottom-items hello)

(def state (atom {
                  :components (cycle ["bottom-items" "hello"])

                  :top-items #{"play"
                               "stop"
                               "apple"
                               "amazon"
                               "edge"
                               }
                  :list-items #{"chrome"
                                "firefox"
                                "google"
                                "opera"}
                      }))


(add-watch state nil (fn [& args] (main)))

(defn pos [e]
  (let [o (.. (js/$ e) offset)]
    [(.-top o) (.-left o)]))

(defn translate [x y]
  (str "translate(" x "px," y "px)"))

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
      (duration 2000)
      (style "opacity" 0)
      (on "end"
          #(.. parent
               (transition)
               (duration 2000)
               (style "width" "0px")
               (style "padding-left" "0px")
               (style "padding-right" "0px")
               (remove)))))

(defn top-bar-item-enter [parent]
  (println "top bar enter")
  (let [{:keys [padding-left padding-right width]} (style parent)]
    (.. parent
        (style "width" 0)
        (style "padding-left" 0)
        (style "padding-right" 0)
        (transition)
        (duration 2000)
        (style "width" (str width "px"))
        (style "padding-left" (str padding-left "px"))
        (style "padding-right" (str padding-right "px"))
        (on "end" (fn []
                    (.. parent
                        (select "i")
                        (transition)
                        (duration 2000)
                        (style "opacity" 1)
                        ))))))

(defn flying [parent ns enter-selection exit-selection]
  (.. exit-selection
      (selectAll "*")
      interrupt)

  (println "flying")
  (animation/set-animation-active ctx [ns :override])
  (let [{:keys [padding-left padding-right width]} (style parent)
        i1 (.. parent (select "i"))
        i0 (.. exit-selection (select "i"))
        [t0 l0] (pos (.. i0 node))
        ]
    (.. i0
        (style "color" "green")
        (on "click" nil))
    (.. parent
        (style "padding-left" 0)
        (style "padding-right" 0)
        (style "width" 0)
        transition
        (duration 2000)
        (style "width" (str width "px"))
        (style "padding-left" (str padding-left "px"))
        (style "padding-right" (str padding-right "px"))
        (on "end" (fn []
                    (let [
                          [t1 l1] (pos (.. i1 node))
                          t (- (- t1 t0))
                          l (- (- l1 l0 4))]
                      (.. i1
                          (style "transform" (str "translate(" l "px," t "px)"))
                          (style "z-index" 1)
                          (style "opacity" 1)
                          (transition)
                          (duration 2000)
                          (style "transform" "translate(0px,0px)")
                          (on "end" (fn []
                                      (..
                                       exit-selection
                                       (transition)
                                       (duration 2000)
                                       (style "height" "0")
                                       (style "padding-top" "0")
                                       (style "padding-bottom" "0")
                                       (on "end" #(animation/set-animation-active ctx [ns :override]))
                                       remove)
                                      )))
                      (.. i0 (style "visibility" "hidden"))

                      ))))))

(defn bottom-item-exit [ns parent]
  (println "item exit")
  (when (not (animation/animation-active? ctx [ns :override]))
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
                        (remove)
                        ))))))

(defn fader [parent enter-selection exit-selection]
  ;; Swap enter and exit in DOM so that other transitions in exit
  ;; do not interfere with the swapping transforms
  (.. parent node (appendChild (.. exit-selection node)))

  (let [get-height  #(.. % (style "height"))]
    (.. exit-selection
        (selectAll "*")
        (on "click" nil))
    (.. parent
        (style "height" (get-height enter-selection)))
    (.. enter-selection
        (style "opacity" 0)
        (transition)
        (duration 1000)
        (style "opacity"1)
        )
    (.. exit-selection
        (style "transform" (str "translateY(-" (get-height enter-selection) ")"))
        (style "opacity" 1)
        (transition "temp")
        (duration 1000)
        (style "opacity" 0)
        (on "end" (fn []
                    (.. exit-selection
                        (style "transform" "translateY(-1px)"))
                    (.. exit-selection
                        remove)
                    ))))
  )

(defn top-bar []
  [:div.ui.top.attached.menu {:join #(.. % (style "height" "6em"))}
   (for [n (:top-items @state)]
     [:div.ui.icon.item
      {:id n
       :click (fn [] (swap! state update-in [:top-items] #(set (remove #{n} %))))
       :exit (fn [selection] (animation/on-exit ctx (keyword "dock" n) selection top-bar-item-exit))
       :enter (fn [selection] (let [ns (keyword "flying" n)]
                               (animation/on-enter ctx ns selection top-bar-item-enter)
                               (animation/on-both ctx ns (partial flying selection ns))))}
      [:i.huge.icon {:id n
                     :join #(..  % (classed n true)
                               (style "color" "red"))
                     :enter #(.. % (style "opacity" 0))}]])])

(defn bottom-items []
  [:div.ui.bottom.attached.segment
   {:id "bottom-items"
    :join #(.. % (style "margin-bottom" 0))
    :enter (fn [selection]
             (println "bottom-items enter")
             (animation/on-enter ctx "fader" selection (fn [sel])))
    :exit (fn [selection]
            (println "bottom-items exit")
            (animation/on-exit ctx "fader" selection (fn [sel] (.. sel remove))))
    }
   (for [n (:list-items @state)]
     [:div.ui.vertical.segment
      {:id n
       :enter #(.. % (style "opacity" 0)
                   (transition)
                   (style "opacity" 1)
                   (attr "class" "ui vertical segment"))
       :exit (fn [selection] (let [ns (keyword "flying" n)]
                              (animation/on-exit ctx (keyword "flying" n) selection (partial bottom-item-exit ns))))}
       [:div.ui.icon.item>i.huge.icon
        {:click (fn []
                  (swap! state update-in [:list-items] #(set (remove #{n} %)))
                  (swap! state update-in [:top-items] conj n))
         :join #(.. % (classed n true))}
        ]
      ])])

(defn hello []
  [:div.ui.bottom.attached.segment
   {:id "hello"
    :join #(.. % (style "margin-bottom" 0))
    :enter (fn [selection]
             (animation/on-enter ctx "fader" selection (fn [sel])))
    :exit (fn [selection]
            (animation/on-exit ctx "fader" selection (fn [sel] (.. sel remove))))
    }
   [:h {:join #(.. % (text (repeat 100 "hello")))}]])

(defn root []
  [:div.ui.container
   (top-bar)
   [:div.ui.container.test
    {:join (fn [selection]
             (animation/on-both ctx "fader" (partial fader selection))
             (.. selection (style "height" "auto"))
             )
     }
    (case (-> @state :components first)
      "hello" (hello)
      "bottom-items" (bottom-items)
      )]
   [:button {:click (fn [] (swap! state update :components next))
             :join  #(.. % (text "Swap"))
             }]
     ])

(defn main []
  (println "***")
  (animation/clear ctx)
  (render
   (.. js/d3 (select "#app"))
   (root))
  (animation/play ctx))

(main)
