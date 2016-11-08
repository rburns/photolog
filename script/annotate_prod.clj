(require 'cljs.build.api)

(cljs.build.api/build "src/photolog"
                      {:main 'photolog.annotate.cli
                       :output-to "target/annotate.js"
                       :output-dir "target/out"
                       :target :nodejs
                       :optimizations :simple
                       :parallel-build true
                       :source-map "target/annotate.js.map"})
