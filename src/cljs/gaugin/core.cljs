(ns gauguin.core
  (:require [strokes :refer [d3]]
            [cljs.core.async :refer [put! chan <! >! alts! timeout close!] :as async]
            [chord.client :refer [ws-ch]]
            [gauguin.data :as data])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(strokes/bootstrap)

(enable-console-print!)
(def uri (goog.Uri. js/location.href))

(def ssl? (= (.getScheme uri) "https"))

(def app-state (atom {:bookmarks []
                      :user {:email nil :token-status nil}
                      :ws []}))

(def socket-url (str (if ssl? "wss://" "ws://")
                     (.getDomain uri)
                     (when (= (.getDomain uri) "localhost")
                       (str ":" (.getPort uri)))
                     "/data/ws"))


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


(defn draw-fdg
  "Draw force-directed graph"
  [data frame]
  (let [width 1080
        height 920
        color (-> d3 .-scale .category10)
        force (-> d3 .-layout .force (.charge -100)  (.linkDistance 20) (.size [width height]))
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
                           :r "3"})
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


#_(draw-reingold tree-data "#the-canvas")

(draw-fdg data/graph-data-1 "#the-canvas-2")
(draw-fdg data/graph-data-2 "#the-canvas-3")
(draw-fdg data/graph-data-3-b "#the-canvas-4")


(defn connect-to-server
  "Build websocket connection to remote server"
  [socket-url]
  (go
    (let [{:keys [ws-channel error] :as ws-conn} (<! (ws-ch socket-url))]
      (>! ws-channel {:topic :graph :data :graph-3})
      (loop [in-msg (<! ws-channel)]
        (when in-msg
          (println "incoming message"))))))


(connect-to-server "http://localhost:8091/data/ws")
