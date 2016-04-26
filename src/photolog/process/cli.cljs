(ns photolog.process.cli
  (:require [cljs.pprint :refer [pprint]]
            [photolog.process.core :refer [process-photos]]
            [photolog.process.node-deps :refer [resolve-path process-argv file-exists-sync
                                                read-file-sync]]))

(def defaults
  ""
  {:img-src-dir   nil,
   :img-out-dir   nil,
   :metadata-path nil,
   :href-prefix   nil,
   :exif-props    ["CreateDate" "ExposureTime" "ScaleFactor35efl" "FocalLength" "LensType"
                   "Aperture" "ISO" "Model" "ImageWidth" "ImageHeight"]
   :breakpoints   [[:tiny 200] [:small 556] [:medium 804] [:large 1000]]})

(defn with-resolved-paths
  ""
  [config]
  (assoc config :img-src-dir (resolve-path (:img-src-dir config))
                :img-out-dir (resolve-path (:img-out-dir config))
                :metadata-path (resolve-path (:metadata-path config))
                :href-prefix (resolve-path (:href-prefix config))))

(defn parsed-config
  ""
  [config-path]
  (if (file-exists-sync config-path)
    (try (-> (.parse js/JSON (read-file-sync config-path))
             (js->clj :keywordize-keys true)
             with-resolved-paths)
         (catch js/Error e (println (str config-path " is not valid JSON."))))
    (println (str config-path " does not exist."))))

(defn merged-config
  ""
  [config default-config]
  (if (some? config) (merge default-config config) nil))

(defn valid-config
  ""
  [config]
  config)

(defn config-with-defaults
  ""
  [default-config]
  (if (>= (count process-argv) 3)
    (-> (resolve-path (last process-argv))
        parsed-config
        (merged-config default-config)
        valid-config)
    (println "Please provide a config file.")))

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
