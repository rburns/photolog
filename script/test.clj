(require 'cljs.build.api)

(cljs.build.api/watch (cljs.build.api/inputs "src" "test")
                      {:main 'test-runner
                       :output-to "target/test.js"
                       :output-dir "target/out"
                       :target :nodejs
                       :optimizations :none
                       :parallel-build true
                       :source-map true})
