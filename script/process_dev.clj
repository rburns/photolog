(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra])

(ra/start-figwheel!
  {:figwheel-options {:server-logfile "/tmp/figwheel_server.log"}
   :build-ids ["process-dev"]
   :all-builds [{:id "process-dev"
                 :figwheel true
                 :source-paths ["src"]
                 :compiler {:main 'photolog.process.main
                            :output-to "process/main.js"
                            :output-dir "process/out"
                            :target :nodejs
                            :parallel-build true}}]})

(ra/cljs-repl)
