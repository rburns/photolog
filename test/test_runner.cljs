(ns test-runner
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :refer-macros [run-tests]]
            [photolog.process.core-test]))

(nodejs/enable-util-print!)

(run-tests 'photolog.process.core-test)

(set! *main-cli-fn* #())
