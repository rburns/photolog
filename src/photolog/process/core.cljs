(ns photolog.process.core
  (:require [clojure.string :refer [join]]
            [cognitect.transit :as transit]
            [photolog.process.node-deps :refer [resolve-path exec-sync sharp write-file-sync
                                                path-basename path-extension write-stdout
                                                file-read-stream file-write-stream]]
            [photolog.process.html :refer [write-html!]]))

(defn exif-data
  ""
  [path props]
  (let [props     (join " " (map #(str "-" %) props))
        exiftool  (str "exiftool -j " props " -ext JPG -fileOrder DateTimeOriginal " path)]
    (try
      (js->clj (.parse js/JSON (exec-sync exiftool)))
      (catch :default error (println error)))))

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

(defn with-placeholder-text
  [photo]
  (cond-> photo
    (empty? (:lens photo)) (assoc :lens "unknown lens")
    (empty? (:model photo)) (assoc :model "unknown camera")))

(defn with-custom-exif-transforms
  [transforms photo]
  (loop [photo photo
         t     0]
    (if (> (count transforms) t)
      (let [transform (nth transforms t)
            t-key     (nth transform 0)
            t-from    (nth transform 1)
            t-to      (nth transform 2)]
        (recur (if (= (photo t-key) t-from) (assoc photo t-key t-to) photo) (inc t)))
      photo)))

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

(defn sizes-element
  [prefix source-path breakpoint]
  {:label (name (first breakpoint))
   :href (str prefix "/" (output-file source-path (first breakpoint)))})

(defn with-sizes
  [prefix breakpoints photo]
  (assoc photo :sizes (concat (map (partial sizes-element prefix (:file photo)) breakpoints)
                              [{:label "original"
                                :href (str prefix "/" (path-basename (:file photo))
                                           (path-extension (:file photo)))}])))

(defn print-feedback
  ""
  [error info]
  (if (some? error) (println (str "Image resize error: " error)) (write-stdout ".")))

(defn resize
  ""
  [source-path output-dir breakpoint]
  (let [label        (first breakpoint)
        width        (.floor js/Math (last breakpoint))]
    (try
      (-> (sharp source-path)
          (.resize width nil)
          (.quality 95)
          (.withoutChromaSubsampling)
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

(defn write-output!
  [format path data template]
  (condp = format
    :transit (write-transit! path data)
    :html   (write-html! path data template)))

(defn process-photos
  ""
  [config]
  (let [transform   (comp (map with-pretty-keys)
                          (map with-placeholder-text)
                          (map (partial with-custom-exif-transforms (:exif-transforms config)))
                          (map with-height-scale)
                          (map (partial with-href (:href-prefix config)))
                          (map (partial with-sizes (:href-prefix config) (:breakpoints config)))
                          (map (partial with-srcset (:href-prefix config) (:breakpoints config))))
        output      (into [] transform (exif-data (:img-src-dir config) (:exif-props config)))]
    (doseq [photo output]
      (resize-with-breakpoints! (:breakpoints config) (:img-out-dir config) photo))
    (doseq [photo output]
      (copy-original! (:img-out-dir config) photo))
    (write-output! (:metadata-format config) (:metadata-path config) output (:html-tmpl config))))
