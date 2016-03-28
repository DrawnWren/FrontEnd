(defproject frontend "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [aleph "0.4.1-beta4"]
                 [org.omcljs/om "1.0.0-alpha31"]
                 [com.stuartsierra/component "0.3.1"]
                 [bidi "2.0.4"]
                 [liberator "0.14.0"]
                 [org.clojure/core.async "0.2.374"]]
  :main frontend.core
  :target-path "target/%s"
  :profiles {:dev {:source-paths ["dev"]
                   :plugins [[lein-cljsbuild "1.1.3"]
                             [lein-figwheel "0.5.0"]]
                   :dependencies [[reloaded.repl "0.2.1"]]
                   :cljsbuild {:builds [{:source-paths ["src" "dev"]
                                         :compiler {:main frontend.client
                                                    :output-to "resources/public/js/app.js"
                                                    :output-dir "resources/public/js"
                                                    :asset-path "js"
                                                    :figwheel true
                                                    :optimizations :none
                                                    :recompile-dependents true
                                                    :source-map true}}]} }
             :uberjar {:aot :all}})
-
