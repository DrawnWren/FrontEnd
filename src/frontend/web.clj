(ns frontend.web
  (:require [bidi.ring :refer [->ResourcesMaybe make-handler]]
            [liberator.core :refer [defresource]]))

(defn home [req]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "hello!"})

(defresource index
  :available-media-types ["text/plain"])

(def handler
  (make-handler ["/" (->ResourcesMaybe {:prefix "public/"})]))
