(ns photolog.process.main
  (:require [cljs.nodejs :as node]
            [clojure.string :refer [join]]
            [cognitect.transit :as t]))

(defn exif-data
  []
  (let [exec-sync  (.-execSync (node/require "child_process"))
        img-dir    "../my-photolog/photos/"
        exif-props (join " " ["-CreateDate" "-ExposureTime" "-ScaleFactor35efl" "-FocalLength"
                              "-LensType" "-Aperture" "-ISO" "-Model" "-ImageWidth" "-ImageHeight"])
        exiftool   (str "exiftool -j " exif-props " ./" img-dir "*.JPG")]
    (js->clj (.parse js/JSON (exec-sync exiftool)))))

(defn transform-keys
  [photo]
  (let [key-map {"CreateDate"       :created           "ExposureTime"     :exposure
                 "ScaleFactor35efl" :focal-lenth-scale "FocalLength"      :focal-length
                 "LensType"         :lens              "Aperture"         :aperture
                 "ISO"              :iso               "Model"            :model
                 "ImageWidth"       :width             "ImageHeight"      :height
                 "SourceFile"       :file}
        transform-key (fn [kv] [(get key-map (first kv)) (last kv)])]
    (into {} (map transform-key (into [] photo)))))

(defn add-height-scale
  [photo]
  (assoc photo :height-scale (/ (:height photo) (:width photo))))

(defn write-transit
  [path data]
  (let [write-file-sync (.-writeFileSync (node/require "fs"))
        output          (t/write (t/writer :json) data)]
    (write-file-sync path output)))

(defn- main
  []
  (let [exif-data (map add-height-scale (map transform-keys (exif-data)))]
    (write-transit "public/photos.json" exif-data)))

(enable-console-print!)

(set! *main-cli-fn* main)
