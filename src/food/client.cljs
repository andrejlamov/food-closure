(ns food.client
  (:require [cljsjs.semantic-ui]
            [cljsjs.d3]
            [cljsjs.jquery]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts!]]
            [food.render :as r :refer [render transform]]
            [food.macros :refer [d3]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def state (atom {:top-items #{
                               ;; "chrome"
                               }

                  :list-items #{"chrome"
                                "firefox"
                                "safari"
                                "edge"
                                "opera"}}))

(defn pos [e]
  (let [o (.. (js/$ e) offset)]
    [(.-top o) (.-left o)]))

(def input-chan (chan))
(def our-pub (async/pub input-chan :topic))

(defn reg [topic chan]
  (async/sub our-pub topic chan))

(defn rpc [topic cb]
  (let [c (chan)]
    (put! input-chan {:topic topic :pid c})
    (go (cb (<! c)))))

(declare root)
(defn top-bar []
  [:div.ui.top.attached.menu {:join (d3 (style "height" "6em"))}
   (for [n (:top-items @state)]
     [:div.ui.icon.item
      {:id n
       :enter (d3 (each (fn []
                          (this-as this
                                   (let [self (.. js/d3 (select this))
                                         lp   (.. self (style "padding-left"))
                                         rp   (.. self (style "padding-right"))]
                                     (.. self
                                         (style "padding-left" 0)
                                         (style "padding-right" 0)
                                         (style "width" 0)
                                         transition
                                         (duration 1500)
                                         (style "width" "6em")
                                         (style "padding-left" lp)
                                         (style "padding-right" rp)
                                         (on "end" #(rpc (str "exit/list-item/" n)
                                                         (fn [e]
                                                           (let [
                                                                 i1       (.. self (select "i"))
                                                                 i0       (.. e (select "i"))
                                                                 [t1 l1] (pos (.. i1 node))
                                                                 [t0 l0] (pos (.. i0 node))
                                                                 t (- (- t1 t0))
                                                                 l (- (- l1 l0 4 ))
                                                                 ]
                                                             (.. i1
                                                                 (style "transform" (str "translate(" l "px," t "px)"))
                                                                 (style "z-index" 1)
                                                                 (style "opacity" 1)
                                                                 (transition)
                                                                 (duration 1000)
                                                                 (style "transform" (str "translate(0,0"))
                                                                 (on "end" (fn [] (.. e
                                                                                     (transition)
                                                                                     (duration 1000)
                                                                                     (style "height" "0")
                                                                                     (style "padding-top" "0")
                                                                                     (style "padding-bottom" "0")
                                                                                     remove)))
                                                                 )
                                                             (.. i0 (style "opacity" 0))
                                                             ))))))))))}
      [:i.huge.icon {:id n
                     :join (d3 (classed n true)
                               (style "color" "red")
                               ;; (style "opacity" 1)
                               )
                     :enter (d3 (style "opacity" 0))
                     }]])])

(defn item-list []
  [:div.ui.bottom.attached.segment
   {:join (d3 (style "height" "100%"))}

   (for [n (:list-items @state)]
     [:div.ui.vertical.segment
      {:id n
       :click (fn [d]
                ;; (println "click" n)
                (swap! state update-in [:list-items] (partial remove #{n}))
                (swap! state update-in [:top-items] conj n)
                ;; (println "state" @state)
                (render
                 (.. js/d3 (select "#app"))
                 (root)))

       :enter (d3 (style "opacity" 0)
                  (transition)
                  (style "opacity" 1)
                  (attr "class" "ui vertical segment"))
       :exit  (d3 (each (fn [] (this-as this
                                (let [output-chan (chan)]
                                  (reg (str "exit/list-item/" n) output-chan)
                                  (go (let [{:keys [pid topic]} (<! output-chan)]
                                        (put! pid (.. js/d3 (select this))))))))))}

                                  ;; (let [{:keys [position]} (<! output-chan)
                                  ;;     [t1 l1] position
                                  ;;     [t0 l0] (pos (.. js/d3 (select this) (select "i") node))]

                                  ;; (.. js/d3 (select this)
                                  ;;     (select "i")
                                  ;;     (transition)
                                  ;;     (duration 2000)
                                  ;;     (style "transform" (str "translate(" (- (- l0 l1)) "px," (- (- t0 t1)) "px)"))
                                  ;;     (on "end" (fn []
                                  ;;                 ;; (println "will remove" n)
                                  ;;                 (put! input-chan {:topic (str "end/translate-to-topbar/" n)})
                                  ;;                 (.. js/d3 (select this)
                                  ;;                     remove)
                                  ;;                 (put! input-chan {:topic "end"})))))

      [:div.ui.icon.item>i.huge.icon
       {:join (d3
               (classed n true))}]])])
(defn root []
  [:div.ui.container
   (top-bar)
   (item-list)])

(defn main []
  ;; (println "client main")
  (render
   (.. js/d3 (select "#app"))
   (root)))

(main)

