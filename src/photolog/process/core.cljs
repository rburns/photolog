(ns photolog.process.core
  (:require [cljs.nodejs :as node]
            [clojure.string :refer [join]]
            [cognitect.transit :as t]))

(defn exif-data
  [path props]
  (let [exec-sync (.-execSync (node/require "child_process"))
        props     (join " " (map #(str "-" %) props))
        exiftool  (str "exiftool -j " props " ./" path "*.JPG")]
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

(defn transform
  [photos]
  (into [] (comp (map transform-keys) (map add-height-scale)) photos))

(defn write
  [path data]
  (let [write-file-sync (.-writeFileSync (node/require "fs"))
        output          (t/write (t/writer :json) data)]
    (write-file-sync path output)))

(defn process
  [img-dir props]
  (transform (exif-data img-dir props)))
