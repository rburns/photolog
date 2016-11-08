(require '[clojure.core.async :refer [thread]])
(require 'cljs.build.api)
(require 'cljs.repl)
(require 'cljs.repl.node)

(thread
  (cljs.build.api/watch "src/photolog"
                        {:main 'photolog.annotate.cli
                         :output-to "target/annotate.js"
                         :output-dir "target/out"
                         :target :nodejs
                         :optimizations :none
                         :parallel-build true
                         :source-map true}))

(thread
  (cljs.build.api/watch "src/photolog"
                        {:main 'photolog.process.cli
                         :output-to "target/process.js"
                         :output-dir "target/out"
                         :target :nodejs
                         :optimizations :none
                         :parallel-build true
                         :source-map true}))

(thread
  (cljs.build.api/watch (cljs.build.api/inputs "src" "test")
                        {:main 'test-runner
                         :output-to "target/test.js"
                         :output-dir "target/out"
                         :target :nodejs
                         :optimizations :none
                         :parallel-build true
                         :source-map true}))

(cljs.repl/repl (cljs.repl.node/repl-env)
                :watch "src/photolog"
                :output-dir "target")
