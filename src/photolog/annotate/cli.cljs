(ns photolog.annotate.cli
 (:require [photolog.platform-node :refer [process-argv path-basename]]
           [photolog.annotate.core :refer [valid-command]]))

;; annotate photo.jpg w section Munich
;; annotate photo.jpg w caption "Possible past lives of JFK"
;; annotate photo.jpg d caption
;; annotate photo1.jpg m section photo2.jpg
;; annotate photo.jpg l

(defn command-parser
  ""
  [output input]
  (condp = (:step output)
    nil       (if (= "annotate" (path-basename input)) (assoc output :step :photo) output)
    :photo    (assoc output :photo input :step :command)
    :command  (assoc output :command (keyword input) :step :param)
    :param    (if (nil? (:params output))
                (assoc output :params [(keyword input)])
                (update-in output [:params] conj input))))

(defn parsed-command
  ""
  [command-line]
  (reduce command-parser {} (process-argv)))

(defn- main
  ""
  []
  (let [command (valid-command (parsed-command (process-argv)) println)]
    (println (if command command "Giving up."))))

(enable-console-print!)

(set! *main-cli-fn* main)
