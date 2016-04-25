(require 'cljs.build.api)

(cljs.build.api/watch "src/photolog/process"
                      {:main 'photolog.process.cli
                       :output-to "process/main.js"
                       :output-dir "process/out"
                       :target :nodejs
                       :optimizations :none
                       :parallel-build true})

