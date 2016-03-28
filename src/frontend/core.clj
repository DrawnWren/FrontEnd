(ns frontend.core
  (:require [aleph.http :as http]
            [com.stuartsierra.component :as component]
            [frontend.web :refer [handler]]))

(defn- start-svr [app port]
  (let [server  (http/start-server app {:port port})]
    (println "Starting server on port: " port)
    server ))

(defn stop-svr [server]
  (when server (.close server)))

(defrecord FrontendServer []
    component/Lifecycle

  (start [this]
    (assoc this :server (start-svr #'handler 9009)))

  (stop [this]
    (stop-svr (:server this))
    (dissoc this :server)
    (println "Stopped server.")))

(defn create-system []
  (FrontendServer.))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (.start (create-system)))
