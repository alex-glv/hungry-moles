(ns hungry-moles.dev
  (:use [figwheel-sidecar.repl-api])
  ;; (:require [cljs.build.api]
  ;;           [cemerick.piggieback]
  ;;           [weasel.repl.websocket])
  )

;; (def *out-path* "resources/public/assets/js/out/main.js")

;; (def *opts-map*  {:main "hungry-moles.core"
;;                   :output-to *out-path*
;;                   :output-dir "resources/public/assets/js/out/"
;;                   :cache-analysis true
;;                   :source-map true
;;                   :optimizations :none
;;                   :asset-path "assets/js/out/"})

;; (defn build []
;;   (cljs.build.api/build "src"
;;                         *opts-map*))


;; (defn auto-build []
;;   (cljs.build.api/watch "src"
;;                         *opts-map*))

;; (defn start-repl []
;;   (cemerick.piggieback/cljs-repl 
;;                                  (weasel.repl.websocket/repl-env :port 9001)
;;                                  :asset-path "assets/js/out"
;;                                  :output-dir "resources/public/assets/js/out/"))
(defn start-build []
  (start-figwheel! {:all-builds (figwheel-sidecar.repl/get-project-cljs-builds)})
  (cljs-repl))

