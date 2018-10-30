(require 'cljs.build.api)

(println "Compiling annotate ...")

(cljs.build.api/build "src/photolog"
                      {:main 'photolog.annotate.cli
                       :output-to "target/annotate.js"
                       :output-dir "target/annotate"
                       :target :nodejs
                       :optimizations :simple
                       :parallel-build true
                       :source-map "target/annotate.js.map"})

(println "Compiling process ...")

(cljs.build.api/build "src/photolog"
                      {:main 'photolog.process.cli
                       :output-to "target/process.js"
                       :output-dir "target/process"
                       :target :nodejs
                       :static-fns true
                       :optimizations :simple
                       :parallel-build true
                       :source-map "target/process.js.map"})