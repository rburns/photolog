(require 'cljs.build.api)

(cljs.build.api/watch "src/photolog"
                      {:main 'photolog.process.cli
                       :output-to "target/process.js"
                       :output-dir "target/out"
                       :target :nodejs
                       :optimizations :none
                       :parallel-build true
                       :source-map true})
