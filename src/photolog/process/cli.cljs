(ns photolog.process.cli
  (:require [cljs.pprint :refer [pprint]]
            [photolog.process.core :refer [process-photos]]
            [photolog.process.config :refer [config-with-defaults defaults]]))

(defn- main
  ""
  []
  (let [config (config-with-defaults defaults)]
   (when config
     (println "\nUsing config:\n")
     (pprint  config)
     (process-photos config))))

(enable-console-print!)

(set! *main-cli-fn* main)
