(ns photolog.process.cli
  (:require [cljs.pprint :refer [pprint]]
            [cljs.core.async :as async :refer [<!]]
            [photolog.process.core :refer [process]]
            [photolog.process.config :refer [config-with-defaults defaults]]
            [photolog.process.platform-node :refer [resolve-path process-argv]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn- print-summary
  [summary]
  (println "\nComplete.\n")
  (println (str "processed: " (:count summary) "/" (+ (:count smmary) (count (:errors summary)))))
  (println (str "metadata: " (:cached-metadata summary) " cached, " (count (:fresh summary)) " new"))
  (when (> (count (:fresh summary)) 0)
    (println "")
    (doseq [p (:fresh summary)] (println (:file p))))
  (println (str "\nerrors: " (count (:errors summary)) "\n"))
  (doseq [e (:errors summary)]
    (println (str (:file e) ":\n"))
    (.log js/console "--" (.toString (:error e)))))

(defn- main
  ""
  []
  (if (>= (count process-argv) 3)
    (let [config-path (resolve-path (last process-argv))
          config      (config-with-defaults config-path defaults println)]
      (when config
        (println "\nUsing config:\n")
        (pprint  config)
        (go (print-summary (<! (process config))))))
    (println "Please provide a config file.")))

(enable-console-print!)

(set! *main-cli-fn* main)
