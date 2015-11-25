(defproject hungry-moles "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3297"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [figwheel-sidecar "0.3.7"]]
                 

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.7"]]
  
  :main hungry-moles.dev                  
  :source-paths ["src"]

  :cljsbuild {:builds [ { :id "user" 
                         :source-paths ["src/"]
                         :figwheel true
                         :compiler {:main "hungry-moles.test-impl"
                                    :optimizations :none
                                    :asset-path "assets/js/"
                                    :output-to "resources/public/assets/js/main.js"
                                    :output-dir "resources/public/assets/js/" } } ]}
 :figwheel {
            :http-server-root "public"
            :nrepl-port 7888
            }
 )
