(ns photolog.process.cli
  (:require [cljs.pprint :refer [pprint]]
            [cljs.core.async :as async :refer [<!]]
            [photolog.process.core :refer [process]]
            [photolog.process.config :refer [config-with-defaults defaults]]
            [photolog.process.platform-node :refer [resolve-path process-argv]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn- main
  ""
  []
  (if (>= (count process-argv) 3)
    (let [config-path (resolve-path (last process-argv))
          config      (config-with-defaults config-path defaults println)]
      (when config
        (println "\nUsing config:\n")
        (pprint  config)
        (go (let [summary (<! (process config))]
              (println "\nComplete.\n")
              (println (str "photos: " (+ (:count summary) (count (:errors summary)))))
              (println (str "success: " (:count summary)))
              (println (str "error: " (count (:errors summary)) "\n"))
              (doseq [e (:errors summary)]
                (println (str (:file e) ":\n"))
                (.log js/console "--" (.toString (:error e))))))))
    (println "Please provide a config file.")))

(enable-console-print!)

(set! *main-cli-fn* main)
