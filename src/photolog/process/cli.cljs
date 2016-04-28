(ns photolog.process.cli
  (:require [cljs.pprint :refer [pprint]]
            [photolog.process.core :refer [process-photos]]
            [photolog.process.config :refer [config-with-defaults defaults]]
            [photolog.process.node-deps :refer [resolve-path process-argv]]))

(defn- main
  ""
  []
  (if (>= (count process-argv) 3)
    (let [config-path (resolve-path (last process-argv))
          config      (config-with-defaults config-path defaults println)]
      (when config
        (println "\nUsing config:\n")
        (pprint  config)
        (process-photos config)))
    (println "Please provide a config file.")))

(enable-console-print!)

(set! *main-cli-fn* main)
