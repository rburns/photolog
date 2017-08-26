(require 'cljs.build.api)

(println "Compiling tests ...")

(cljs.build.api/build (cljs.build.api/inputs "src" "test")
                      {:main 'test-runner
                       :output-to "target/test.js"
                       :output-dir "target/test"
                       :target :nodejs
                       :optimizations :simple
                       :parallel-build true
                       :source-map "target/test.js.map"})
