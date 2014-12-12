(ns gauguin.core
  (:require [strokes :refer [d3]]
            [gauguin.data :as data]
            [figwheel.client :as fw]))

(strokes/bootstrap)

(enable-console-print!)

#_(fw/start {:on-jsload (fn [] (print "reloaded"))})


(def tree-data
  (clj->js
   {:name "p1"
    :parent nil
    :children [{:name "u1"
                :parent "p1"}
               {:name "p2"
                :parent "p1"
                :children [{:name "p4"
                            :parent "p2"}
                           {:name "p5"
                            :parent "p5"}]}
               {:name "p3"
                :parent "p1"}
               {:name "p4"
                :parent "p1"}]}))

(def graph-data
  (clj->js
   {:nodes [{:name "p1" :group 0}
            {:name "p2" :group 1}
            {:name "p3" :group 1}
            {:name "p4" :group 1}
            {:name "p5" :group 1}
            {:name "p6" :group 1}
            {:name "p7" :group 1}
            {:name "p8" :group 1}
            {:name "p9" :group 1}
            {:name "p10" :group 1}
            {:name "p11" :group 1}
            {:name "p12" :group 1}
            {:name "p13" :group 1}
            {:name "p14" :group 1}
            {:name "p15" :group 1}
            ]
    :links [{:source 1 :target 0 :value 1}
            {:source 2 :target 0 :value 1}
            {:source 3 :target 1 :value 1}
            {:source 4 :target 1 :value 1}
            {:source 5 :target 0 :value 1}
            {:source 6 :target 0 :value 1}
            {:source 7 :target 0 :value 1}
            {:source 8 :target 0 :value 1}
            {:source 9 :target 0 :value 1}
            {:source 10 :target 3 :value 1}
            {:source 11 :target 3 :value 1}
            {:source 12 :target 0 :value 1}
            {:source 13 :target 0 :value 5}
            ]}))


(defn draw-reingold
  "Draw tree using Reingold-Tilford Algorithm"
  [data]
  (let [width 1080
        height 920
        tree (-> d3 .-layout .tree (.size [(- height 50) width]))
        diagonal (-> d3 .-svg .diagonal (.projection #(clj->js [(* 0.6 (.-x %)) (* 0.6 (.-y %))])))
        svg (-> d3 (.select "#the-canvas")
                (.attr {:width width :height height})
                (.append "g")
                (.attr {:transform (str "translate(" (/ width 4) ",20)")}))
        nodes (.nodes tree data)
        links (.links tree nodes)
        link (-> svg
              (.selectAll "path.link")
              (.data links)
              .enter
              (.append "path")
              (.attr {:class "link"
                      :d diagonal}))
        node (-> svg
              (.selectAll "g.node")
              (.data nodes)
              (.enter)
              (.append "g")
              (.attr {:class "node"
                      :transform #(str "translate(" (* 0.6 (.-x %)) "," (* 0.6 (.-y %)) ")")}))]
    (do
      (-> node
          (.append "circle")
          (.attr {:r 4.5}))
      (-> node
          (.append "text")
          (.attr {:dx 3
                  :dy #(if (.-children %) -8 8)
                  :text-anchor #(if (.-children %) "end" "start")})
          (.style {:font-size "10px"})
          (.text #(.-name %)))
      (-> d3
          (.select (.-frameElement js/self))
          (.style {:height (str (- height 50) "px")
                   :width (str width "px")})))))


(defn draw-fdg
  "Draw force-directed graph"
  [data frame]
  (let [width 1080
        height 920
        color (-> d3 .-scale .category10)
        force (-> d3 .-layout .force (.charge -200) (.linkDistance 30) (.size [width height]))
        svg (-> d3
                (.select frame)
                (.attr {:width width
                        :height height}))]
    (-> force
        (.nodes (.-nodes data))
        (.links (.-links data))
        .start)
    (let [link (-> svg
                   (.selectAll ".link")
                   (.data (.-links data))
                   .enter
                   (.append "line")
                   (.attr {:class "link"})
                   (.style {:stroke-with (fn [d] (.sqrt js/Math (.-value d)))}))
          node (-> svg
                   (.selectAll ".node")
                   (.data (.-nodes data))
                   .enter
                   (.append "circle")
                   (.attr {:class "node"
                           :r "4"})
                   (.style {:fill (fn [d] (color (.-group d)))})
                   (.call (.-drag force)))]
      (do
        (-> node (.append "title") (.text (fn [d] (.-name d))))
        (-> force
            (.on "tick"
                 (fn []
                   (-> link
                       (.attr
                        {:x1 #(-> % .-source .-x)
                         :y1 #(-> % .-source .-y)
                         :x2 #(-> % .-target .-x)
                         :y2 #(-> % .-target .-y)}))
                   (-> node
                       (.attr {:cx #(.-x %)
                               :cy #(.-y %)})))))))))


(draw-reingold tree-data)

(draw-fdg data/graph-data-1 "#the-canvas-2")
(draw-fdg data/graph-data-2 "#the-canvas-3")
(draw-fdg data/graph-data-3-b "#the-canvas-4")
