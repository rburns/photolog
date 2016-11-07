(require 'cljs.repl)
(require 'cljs.build.api)
(require 'cljs.repl.node)

(cljs.build.api/build "src/photolog"
  {:main 'photolog.process.core
   :output-to "target/repl.js"
   :verbose true})

(cljs.repl/repl (cljs.repl.node/repl-env)
  :watch "src/photolog"
  :output-dir "target")
