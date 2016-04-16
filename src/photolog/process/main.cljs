(ns photolog.process.main
  (:require [photolog.process.core :as p]))

(defn- main
  []
  (let [img-dir   "../my-photolog/photos/"
        props     ["CreateDate" "ExposureTime" "ScaleFactor35efl" "FocalLength" "LensType"
                   "Aperture" "ISO" "Model" "ImageWidth" "ImageHeight"]]
    (p/write-transit "public/photos.json" (p/transform (p/exif-data img-dir props)))))

(enable-console-print!)

(set! *main-cli-fn* main)
