(ns gauguin.core
  (:gen-class :main true)
  (:require [clojure.java.io :as io]
            [compojure.route :refer [resources]]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.handler :refer [site api]]
            [org.httpkit.server :refer [with-channel on-receive on-close run-server]]))


(defn read-graph
  "Read data from edn file"
  [graph]
  (->> (str "data/" (name graph) ".edn")
            slurp
            read-string))


(defn dispatch-request
  "Dispatch incoming requests"
  [{:keys [topic data]}]
  (case topic
    :graph (read-graph data)
    :unrelated))


(defn ws-handler
  "Handle incoming websocket requests"
  [request]
  (with-channel request channel
    (on-close channel (fn [msg] (println "Channel closed!")))
    (on-receive channel (fn [msg]
                          (read-graph (dispatch-request (read-string msg)))))))


(defroutes handler
  (resources "/")
  (GET "/data/ws" [] ws-handler)
  (GET "/*" [] (io/resource "public/index.html")))


(defn -main [& args]
  (run-server (site #'handler) {:port 8091 :join? false})
  (println "Server up and running!")
  (println  "Visit http://localhost:8091"))


(comment

  (dispatch-request {:topic :graph :data :graph-3})


  )
