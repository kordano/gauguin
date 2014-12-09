(defproject gauguin "0.1.0-SNAPSHOT"

  :description "bookmark app"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"

  :source-paths ["src/cljs" "src/clj"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2411"]
                 [net.drib/strokes "0.5.1"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :cljsbuild
  {:builds
   [{:source-paths ["src"]
     :compiler
     {:output-to "resources/public/js/main.js"
      :optimizations :simple}}]})
