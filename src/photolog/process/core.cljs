(ns photolog.process.core
  (:require [clojure.string :refer [join]]
            [cognitect.transit :as transit]
            [photolog.process.node-deps :refer [resolve-path exec-sync sharp write-file-sync
                                                path-basename path-extension write-stdout
                                                file-read-stream file-write-stream]]))

(defn exif-data
  ""
  [path props]
  (let [props     (join " " (map #(str "-" %) props))
        exiftool  (str "exiftool -j " props " " path "/*.JPG")]
    (js->clj (.parse js/JSON (exec-sync exiftool)))))

(defn with-pretty-keys
  ""
  [photo]
  (let [key-map {"CreateDate"       :created   "ExposureTime" :exposure
                 "ScaleFactor35efl" :efl-scale "FocalLength"  :focal-length
                 "LensType"         :lens      "Aperture"     :aperture
                 "ISO"              :iso       "Model"        :model
                 "ImageWidth"       :width     "ImageHeight"  :height
                 "SourceFile"       :file}
        transform-key (fn [kv] [(get key-map (first kv)) (last kv)])]
    (into {} (map transform-key (into [] photo)))))

(defn with-height-scale
  ""
  [photo]
  (assoc photo :height-scale (/ (:height photo) (:width photo))))

(defn output-file
  [source-path label]
  (str (path-basename source-path)
       (when label (str "-" (name label)))
       (path-extension source-path)))

(defn output-path
  [output-dir source-path label]
  (str output-dir "/" (output-file source-path label)))

(defn srcset-element
  [prefix source-path breakpoint]
  (str prefix "/" (output-file source-path (first breakpoint)) " " (last breakpoint) "w"))

(defn srcset
  [prefix source-path breakpoints]
  (let [element  (partial srcset-element prefix source-path)]
   (reduce #(conj %1 (element %2)) [] breakpoints)))

(defn with-srcset
  [prefix breakpoints photo]
  (assoc photo :srcset (join "," (srcset prefix (:file photo) breakpoints))))

(defn with-href
  [prefix photo]
  (assoc photo :href (str prefix "/" (path-basename (:file photo)) (path-extension (:file photo)))))

(defn print-feedback
  ""
  [error info]
  (if (some? error) (println (str "Image resize error: " error)) (write-stdout ".")))

(defn resize
  ""
  [source-path output-dir breakpoint]
  (let [label        (first breakpoint)
        longest-edge (last breakpoint)
        width        (.floor js/Math longest-edge)
        height       (.floor js/Math (/ (* longest-edge 2) 3))]
    (try
      (-> (sharp source-path)
          (.resize width height)
          (.toFile (output-path output-dir source-path label) print-feedback))
      (catch :default error (print-feedback error nil)))))

(defn resize-with-breakpoints!
  ""
  [breakpoints output-dir photo]
  (doseq [breakpoint breakpoints] (resize (:file photo) output-dir breakpoint))
  photo)

(defn copy-original!
  [output-dir photo]
  (.pipe (file-read-stream (:file photo))
         (file-write-stream (output-path output-dir (:file photo) nil))))

(defn write-transit!
  ""
  [path data]
  (write-file-sync path (transit/write (transit/writer :json) data)))

(defn process-photos
  ""
  [config]
  (let [transform   (comp (map with-pretty-keys)
                          (map with-height-scale)
                          (map (partial with-href (:href-prefix config)))
                          (map (partial with-srcset (:href-prefix config) (:breakpoints config))))
        output      (into [] transform (exif-data (:img-src-dir config) (:exif-props config)))]
    (doseq [photo output]
      (resize-with-breakpoints! (:breakpoints config) (:img-out-dir config) photo))
    (doseq [photo output]
      (copy-original! (:img-out-dir config) photo))
    (write-transit! (:metadata-path config) output)))
