(ns test-runner
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :refer-macros [run-tests]]
            [photolog.annotate.core-test]
            [photolog.process.core-test]
            [photolog.process.config-test]
            [photolog.platform-node-test]))

(nodejs/enable-util-print!)

(defn- main
  []
  (run-tests 'photolog.annotate.core-test)
  (run-tests 'photolog.platform-node-test)
  (run-tests 'photolog.process.config-test)
  (run-tests 'photolog.process.core-test))

(set! *main-cli-fn* main)

