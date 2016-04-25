(require 'cljs.build.api)

(cljs.build.api/build "src/photolog/process"
                      {:main 'photolog.process.cli
                       :output-to "process/main.js"
                       :output-dir "process/out"
                       :target :nodejs
                       :optimizations :simple
                       :parallel-build true})
