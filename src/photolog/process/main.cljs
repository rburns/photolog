(ns photolog.process.main
  (:require [cljs.nodejs :as node]
            [clojure.string :refer [join]]))

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

(defn write-cljs
  [path namespace & vars]
  (let [write-file-sync (.-writeFileSync (node/require "fs"))
        namespace       (str "(ns " namespace ")")
        vars            (map #(str "(def "  (first %) " " (last %) ")") (partition 2 vars))
        out             (str namespace (first vars))]
    (write-file-sync path out)))

(defn- main
  []
  (let [exif-data (map add-height-scale (map transform-keys (exif-data)))]
    (write-cljs "public/photos.cljs" 'photolog.photos 'photos exif-data)))

(enable-console-print!)

(set! *main-cli-fn* main)
