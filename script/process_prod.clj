(require 'cljs.build.api)

(cljs.build.api/build "src/photolog/process"
                      {:main 'photolog.process.cli
                       :output-to "process/main.js"
                       :output-dir "process/out"
                       :target :nodejs
                       :static-fns true
                       :optimizations :simple
                       :parallel-build true})
