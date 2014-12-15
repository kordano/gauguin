(defproject gauguin "0.1.0-SNAPSHOT"

  :description "fun with d3"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"

  :source-paths ["src/cljs" "src/clj"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2411"]
                 [org.clojure/core.memoize "0.5.6"] ;; needed for figwheel
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 ;; backend
                 [http-kit "2.1.19"]
                 [ring "1.3.1"]
                 [com.cemerick/friend "0.2.1"]
                 [enlive "1.1.5"]
                 [compojure "1.2.1"]

                 ;; frontend
                 [net.drib/strokes "0.5.1"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :main gauguin.core

  :cljsbuild
  {:builds
   [{:source-paths ["src"]
     :compiler
     {:output-to "resources/public/js/compiled/main.js"
      :output-dir "resources/public/js/compiled/out"
      :optimizations :none
      :pretty-print false
      :source-map "main.js.map"}}]})
