(ns photolog.process.main
  (:require [clojure.string :refer [join]]
            [cognitect.transit :as transit]
            [photolog.process.node-deps :refer [resolve-path exec-sync sharp write-file-sync]]))

(defn exif-data
  [path props]
  (let [props     (join " " (map #(str "-" %) props))
        exiftool  (str "exiftool -j " props " " path "/*.JPG")]
    (js->clj (.parse js/JSON (exec-sync exiftool)))))

(defn transform-keys
  [photo]
  (let [key-map {"CreateDate"       :created   "ExposureTime" :exposure
                 "ScaleFactor35efl" :efl-scale "FocalLength"  :focal-length
                 "LensType"         :lens      "Aperture"     :aperture
                 "ISO"              :iso       "Model"        :model
                 "ImageWidth"       :width     "ImageHeight"  :height
                 "SourceFile"       :file}
        transform-key (fn [kv] [(get key-map (first kv)) (last kv)])]
    (into {} (map transform-key (into [] photo)))))

(defn add-height-scale
  [photo]
  (assoc photo :height-scale (/ (:height photo) (:width photo))))

(defn print-resize-error
  ""
  [error info]
  (when (some? error) (println (str "Image resize error: " error))))

(defn resize
  ""
  [source-path output-dir longest-edge]
  (let [width  longest-edge
        height (/ (* longest-edge 2) 3)]
    (fn []
      (try
        (-> (sharp source-path)
            (.resize width height)
            (.toFile "/home/rburns/projects/photolog/test_photo.jpg" print-resize-error))
        (catch :default error (print-resize-error error nil))))))

(defn resize-for-breakpoints
  ""
  [breakpoints output-dir photo]
  (assoc photo :resized (map (partial resize (:file photo)) breakpoints)))

(defn write
  ""
  [path data]
  (write-file-sync path (transit/write (transit/writer :json) data)))

(defn- main
  ""
  []
  (let [img-dir     (resolve-path "../my-photolog/photos/")
        _           (println (str "img-dir: " img-dir))
        props       ["CreateDate" "ExposureTime" "ScaleFactor35efl" "FocalLength" "LensType"
                     "Aperture" "ISO" "Model" "ImageWidth" "ImageHeight"]
        breakpoints [1000 804 556 200]
        output-dir  (resolve-path "./public/img")
        _           (println (str "output-dir: " output-dir))
        transform   (comp (map transform-keys)
                          (map add-height-scale)
                          (map (partial resize-for-breakpoints breakpoints output-dir)))
        output      (into [] transform (exif-data img-dir props))]))

(enable-console-print!)

(set! *main-cli-fn* main)
