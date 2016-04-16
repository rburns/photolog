(ns photolog.process.main
  (:require [photolog.process.core :refer [process write]]))

(defn- main
  []
  (let [img-dir   "../my-photolog/photos/"
        props     ["CreateDate" "ExposureTime" "ScaleFactor35efl" "FocalLength" "LensType"
                   "Aperture" "ISO" "Model" "ImageWidth" "ImageHeight"]]
    (write "public/photos.json" (process img-dir props))))

(enable-console-print!)

(set! *main-cli-fn* main)
