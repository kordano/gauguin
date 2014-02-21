(ns gauguin.core
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as enlive]
            [compojure.route :refer (resources)]
            [compojure.core :refer [GET defroutes]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.java.io :as io]))

                                        ; ring server, only for production
(enlive/deftemplate page
  (io/resource "public/index.html")
  []
  [:body] (enlive/append
            (enlive/html [:script (browser-connected-repl-js)])))


(defroutes site
  (resources "/")
  (GET "/*" req (page)))


(defn start [port]
  (run-jetty #'site {:port port :join? false}))


(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))

(def server (start 8080))
#_(.stop server)
#_(.start server)
