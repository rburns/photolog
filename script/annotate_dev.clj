(require 'cljs.build.api)

(cljs.build.api/watch "src/photolog/annotate"
                      {:main 'photolog.annotate.cli
                       :output-to "target/annotate.js"
                       :output-dir "target/out"
                       :target :nodejs
                       :optimizations :none
                       :parallel-build true
                       :source-map true})
