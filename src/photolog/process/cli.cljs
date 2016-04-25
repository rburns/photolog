(ns photolog.process.cli
  (:require [cljs.pprint :refer [pprint]]
            [photolog.process.core :refer [process-photos]]
            [photolog.process.node-deps :refer [resolve-path]]))

(def default-config
  {:img-src-dir   (resolve-path "../my-photolog/photos/")
   :img-out-dir   (resolve-path "./public/img")
   :metadata-path (resolve-path "./public/photos.json")
   :href-prefix   (resolve-path "/img")
   :exif-props    ["CreateDate" "ExposureTime" "ScaleFactor35efl" "FocalLength" "LensType"
                   "Aperture" "ISO" "Model" "ImageWidth" "ImageHeight"]
   :breakpoints   [[:tiny 200] [:small 556] [:medium 804] [:large 1000]]})

(defn- main
  ""
  []
  (pprint default-config)
  (process-photos default-config))

(enable-console-print!)

(set! *main-cli-fn* main)
