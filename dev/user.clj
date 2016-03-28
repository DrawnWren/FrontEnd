(ns user
  (:require [reloaded.repl :refer [system reset stop]]
            [frontend.core :refer [create-system]]))

(reloaded.repl/set-init! #'create-system)
