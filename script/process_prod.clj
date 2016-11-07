(require 'cljs.build.api)

(cljs.build.api/build "src/photolog/process"
                      {:main 'photolog.process.cli
                       :output-to "target/process.js"
                       :output-dir "target/out"
                       :target :nodejs
                       :static-fns true
                       :optimizations :simple
                       :parallel-build true
                       :source-map "target/process.js.map"})

