(ns gauguin.core
  (:require [strokes :refer [d3]]
            [cljs.core.async :refer [put! chan <! >! alts! timeout close!] :as async]
            [chord.client :refer [ws-ch]]
            [om.core :as om :include-macros true]
            [kioo.om :refer [content set-attr do-> substitute listen remove-attr add-class remove-class]]
            [kioo.core :refer [handle-wrapper]]
            [cljs.reader :refer [read-string] :as read]
            [gauguin.data :as data])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [kioo.om :refer [defsnippet deftemplate]]))

(strokes/bootstrap)

(enable-console-print!)

(def app-state
  (atom
   {:graph-data []
    :reingold-data
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
                  :parent "p1"}]})}))


(defn draw-reingold
  "Draw tree using Reingold-Tilford Algorithm"
  [data frame]
  (let [width 1080
        height 920
        tree (-> d3 .-layout .tree (.size [(- height 50) width]))
        diagonal (-> d3 .-svg .diagonal (.projection #(clj->js [(* 0.6 (.-x %)) (* 0.6 (.-y %))])))
        svg (-> d3 (.select frame)
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


(defn clear-canvas [frame]
  (-> d3
      (.select frame)
      (.select "svg")
      .remove))


(defn draw-fdg
  "Draw force-directed graph"
  [data frame]
  (let [width 1080
        height 920
        color (.. d3 -scale category10)
        force (.. d3 -layout force (charge -100)  (linkDistance 20) (size [width height]))
        svg (.. d3
                (select frame)
                (append "svg")
                (attr {:width width
                        :height height}))]
    (.. force
        (nodes (.-nodes data))
        (links (.-links data))
        start)
    (let [link (.. svg
                   (selectAll ".link")
                   (data (.-links data))
                   enter
                   (append "line")
                   (attr {:class "link"})
                   (style {:stroke-with (fn [d] (.sqrt js/Math (.-value d)))}))
          node (.. svg
                   (selectAll ".node")
                   (data (.-nodes data))
                   enter
                   (append "circle")
                   (attr {:class "node"
                           :r "4"})
                   (style {:fill (fn [d] (color (.-group d)))})
                   (call (.-drag force)))]
      (do
        (.. node (append "title") (text (fn [d] (.-value d))))
        (.. force
            (on "tick"
                 (fn []
                   (.. link
                       (attr
                        {:x1 #(.. % -source -x)
                         :y1 #(.. % -source -y)
                         :x2 #(.. % -target -x)
                         :y2 #(.. % -target -y)}))
                   (.. node
                       (attr {:cx #(.-x %)
                               :cy #(.-y %)})))))))))


;; --- GRAPH VIEW ---

(defsnippet graph-menu-entry "templates/graph.html" [:.graph-menu-entry]
  [owner id]
  {[:.graph-menu-entry-text] (do->
                              (content (str "graph-" id))
                              (listen :on-click
                                      (fn [e]
                                        (go
                                          (>! (om/get-state owner :ws-ch) {:topic :graph :data (keyword (str "graph-" id))})
                                          (om/set-state! owner :selected id)))))})


(deftemplate graph-header "templates/graph.html"
  [app owner state]
  {[:#container-header] (content (str "Force-based Graph: " (:selected state)))
   [:#graph-dropdown-menu] (content (map (partial graph-menu-entry owner) (range 50)))})


(defn force-graph-view
  "Force-based graphs with selection"
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:ws-ch nil
       :selected "0"})
    om/IWillMount
    (will-mount [_]
      (go
        (let [{:keys [ws-channel error] :as ws-conn} (<! (ws-ch "ws://localhost:8091/data/ws"))]
          (if-not error
            (do
              (om/set-state! owner :ws-ch ws-channel)
              (>! ws-channel {:topic :graph :data :graph-0})
              (loop [{msg :message err :error} (<! ws-channel)]
                (if-not err
                  (do (om/transact! app :graph-data (fn [old] (merge old msg)))
                      (go
                        (clear-canvas "#graph-container")
                        (draw-fdg (-> msg vals first) "#graph-container"))
                      (if-let [new-msg (<! ws-channel)]
                        (recur new-msg)))
                  (println "Error: " (pr-str err)))))
            (println "Error")))))
    om/IRenderState
    (render-state [app this]
      (graph-header app owner this))))


(om/root
 force-graph-view
 app-state
 {:target (. js/document (getElementById "graph-container"))})
