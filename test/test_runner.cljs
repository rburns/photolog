(ns test-runner
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :refer-macros [run-tests]]
            [photolog.process.core-test]
            [photolog.process.config-test]
            [photolog.process.platform-node-test]))

(nodejs/enable-util-print!)

(run-tests 'photolog.process.core-test)
(run-tests 'photolog.process.config-test)
(run-tests 'photolog.process.platform-node-test)

(set! *main-cli-fn* #())
