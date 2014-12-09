(ns gauguin.client
  (:require [hiccups.runtime :as hiccupsrt]
            [clojure.browser.repl]
            [cljs.core.async :refer [put! chan <! timeout]]
            [strokes :refer [d3]])
  (:require-macros [hiccups.core :as hiccups]
                   [cljs.core.async.macros :refer [go]]))

(.log js/console "HAIL TO THE LAMBDA!")



;; fire up repl
#_(do
    (def repl-env (reset! cemerick.austin.repls/browser-repl-env
                         (cemerick.austin/repl-env)))
    (cemerick.austin.repls/cljs-repl repl-env))

(strokes/bootstrap)

(def data [63 39 31 53 25 32 175 69 51])

(def margin {:top 50 :right 40 :bottom 50 :left 40})
(def width (- 960 (margin :left) (margin :right)))
(def height (- 500  (margin :top) (margin :bottom)))

; x is a fn: data ↦ width
(def x
  (-> d3
      .-scale
      (.ordinal)
      (.domain (vec (range (count data))))
      (.rangeRoundBands [0 width] 0.2)))

; y is a fn: index ↦ y
(def y
  (-> d3
      .-scale
      (.linear)
      (.domain [(apply max data) 0])
      (.range [height 0])))


(def svg2
  (-> d3
      (.select "body")
      (.append "svg")
      (.attr {:width  (+ width (margin :left) (margin :right))
              :height (+ height (margin :top) (margin :bottom))})
      (.append "g")
      (.attr {:transform (str "translate(" (margin :left) "," (margin :top) ")")})))


; Data ↦ Element
(def bar2
  (-> svg2
      (.selectAll "g.bar")
      (.data data)
      (.enter)
      (.append "g")
      (.attr {:class "bar"
              :transform #(str "translate(" (x %1) "," (- height  (y (data %2))) ")" )})
      (.style {:fill "steelblue"})))


; Data Attributes ↦ Element Attributes
(-> bar2
    (.append "rect")
    (.attr {:height  #(y %)
            :width (.rangeBand x)}))


; Data Attributes ↦ Element Attributes
#_(-> bar2
    (.append "text")
    (.attr {:x  x
            :y  (/ (.rangeBand y) 2)
            :dx -6
            :dy ".35em"
            :text-anchor "end"})
    (.style "fill" "white")
    (.text identity))

#_(-> d3
    (.select "svg")
    (.selectAll "g.bar")
    (.style {:fill "steelblue"}))


#_ (go
  (while true
    (<! (timeout 1000))
    (draw-bar "green")
    (<! (timeout 1000))
    (draw-bar "red")
    (<! (timeout 1000))
    (draw-bar "blue")))
