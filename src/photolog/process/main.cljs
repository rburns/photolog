(ns photolog.process.main
  (:require [photolog.process.core :as p]))

(defn- main
  []
  (let [img-dir "../my-photolog/photos/"
        props   ["CreateDate" "ExposureTime" "ScaleFactor35efl" "FocalLength" "LensType"
                 "Aperture" "ISO" "Model" "ImageWidth" "ImageHeight"]
        data    (map p/add-height-scale (map p/transform-keys (p/exif-data img-dir props)))]
    (p/write-transit "public/photos.json" data)))

(enable-console-print!)

(set! *main-cli-fn* main)
