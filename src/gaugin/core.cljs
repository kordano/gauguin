(ns gauguin.core
  (:require [strokes :refer [d3]]
            [figwheel.client :as fw]))

(strokes/bootstrap)

(enable-console-print!)

(fw/start {
  ;; configure a websocket url if yor are using your own server
  ;; :websocket-url "ws://localhost:3449/figwheel-ws"

  ;; optional callback
  :on-jsload (fn [] (print "reloaded"))

  ;; The heads up display is enabled by default
  ;; to disable it:
  ;; :heads-up-display false

  ;; when the compiler emits warnings figwheel
  ;; blocks the loading of files.
  ;; To disable this behavior:
  ;; :load-warninged-code true
               })

; 26 characters in a vec
(def alphabet (vec "abcdefghijklmnopqrstuwvxyz"))

(def tree-data
  (clj->js
   {:name "s1"
    :parent nil
    :children [{:name "r1"
                :parent "s1"
                :children [{:name "r2"
                            :parent "r1"
                            :children [{:name "r6"
                                        :parent "r2"}
                                       {:name "r7"
                                        :parent "r2"}
                                       {:name "r8"
                                        :parent "r2"}
                                       {:name "r9"
                                        :parent "r2"}
                                       {:name "r10"
                                        :parent "r2"}
                                       ]}
                           {:name "r3"
                            :parent "r1"}]}
               {:name "r4"
                :parent "s1"}
               {:name "r5"
                :parent "s1"}
               {:name "r11"
                :parent "s1"}
               {:name "r12"
                :parent "s1"}
               {:name "r13"
                :parent "s1"}
               {:name "r14"
                :parent "s1"
                :children [
                          {:name "r15"
                            :parent "r14"}
                          {:name "r16"
                            :parent "r14"}
                          {:name "r17"
                            :parent "r14"}
                          {:name "r18"
                            :parent "r14"}
                          {:name "r19"
                            :parent "r14"}
                          {:name "r20"
                            :parent "r14"}
                           ]}
               ]}))

(def width 2000 #_(.. js/window -screen -availWidth))
(def height 2000 #_(.. js/window -screen -availHeight))

(def tree (-> d3 .-layout (.tree) (.size [(- height 50) width])))

(def diagonal (-> d3 .-svg .diagonal (.projection (fn [d] (clj->js [(* 0.6 (.-x d)) (* 0.6 (.-y d))])))))

(def svg (-> d3 (.select "#the-canvas")
      (.attr {:width width :height height})
      (.append "g")
      (.attr {:transform (str "translate(" (/ width 4) ",20)")})))

(def nodes (.nodes tree tree-data))

(def links (.links tree nodes))

(def link (-> svg
              (.selectAll "path.link")
              (.data links)
              (.enter)
              (.append "path")
              (.attr {:class "link"
                      :d diagonal})))

(def node (-> svg
              (.selectAll "g.node")
              (.data nodes)
              (.enter)
              (.append "g")
              (.attr {:class "node"
                      :transform (fn [d] (str "translate(" (* 0.6 (.-x d)) "," (* 0.6 (.-y d)) ")"))})))

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
             :width (str width "px")}))
