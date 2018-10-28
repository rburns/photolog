(ns photolog.process.core
  (:require [clojure.string :refer [join]]
            [cljs.core.async :as async :refer [chan onto-chan <!]]
            [photolog.process.platform-node :refer [stat-path path-basename path-extension exec
                                                    timestamps file-exists-error? symlink-path
                                                    read-dir resize file-does-not-exist-error?
                                                    timestamp-now]]
            [photolog.process.metadata-cache :refer [generate-metadata-cache write-metadata-cache!]]
            [photolog.process.output :refer [write-metadata!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn image?
  ""
  [filename]
  (contains? #{".jpg" ".jpeg" ".png" ".tif" ".tiff"} (.toLowerCase (path-extension filename))))

(defn as-photo
  ""
  [dir-path filename]
  {:file (str dir-path "/" filename)})

(defn with-timestamps
  ""
  [photo]
  (go
    (let [stat (<! (stat-path (:file photo)))]
      (if (some? (:error stat))
        (assoc photo :error (:error stat))
        (merge photo (timestamps stat))))))

(defn with-cached-metadata
  [cache photo]
  (if-let [cached (cache (:file photo) (:file_modified photo))]
    (merge photo (assoc cached :cached-metadata true))
    photo))

(defn output-file
  ""
  [source-path label]
  (str (path-basename source-path)
       (when label (str "-" (name label)))
       (path-extension source-path)))

(defn output-path
  [output-dir source-path label]
  (str output-dir "/" (output-file source-path label)))

(defn should-resize?
  [photo destination]
  (go
    (let [stat (<! (stat-path destination))]
      (or (file-does-not-exist-error? (:error stat))
          (> (:file-modified photo) (:file-modified (timestamps stat)))))))

(defn resize-with-breakpoints!
  ""
  [breakpoints output-dir photo]
  (go
   (doseq [breakpoint breakpoints]
     (let [destination (output-path output-dir (:file photo) (first breakpoint))
           result      (when (<! (should-resize? photo destination))
                         (<! (resize (:file photo) destination breakpoint)))]
       (when (some? (:error result)) (assoc photo :error (:error result)))))
   photo))

(defn link-original!
  [output-dir photo]
  (go
    (let [result (<! (symlink-path (:file photo) (output-path output-dir (:file photo) nil)))]
      (cond
        (file-exists-error? (:error result)) (assoc photo :info "Skipped linking existing file")
        (some? (:error result)) (assoc photo :error (:error result))
        :else photo))))

(defn sizes-element
  ""
  [prefix source-path breakpoint]
  {:label (name (first breakpoint))
   :href (str prefix "/" (output-file source-path (first breakpoint)))})

(defn with-sizes
  ""
  [prefix breakpoints photo]
  (assoc photo :sizes (concat (map (partial sizes-element prefix (:file photo)) breakpoints)
                              [{:label "original"
                                :href (str prefix "/" (path-basename (:file photo))
                                           (path-extension (:file photo)))}])))

(defn srcset-element
  ""
  [prefix source-path breakpoint]
  (str prefix "/" (output-file source-path (first breakpoint)) " " (last breakpoint) "w"))

(defn srcset
  ""
  [prefix source-path breakpoints]
  (let [element  (partial srcset-element prefix source-path)]
   (reduce #(conj %1 (element %2)) [] breakpoints)))

(defn with-srcset
  ""
  [prefix breakpoints photo]
  (assoc photo :srcset (join "," (srcset prefix (:file photo) breakpoints))))

(defn with-exif
  ""
  [props photo]
  (go
    (let [props     (join " " (map #(str "-" %) props))
          exiftool  (str "exiftool -j " props " " (:file photo))
          exif      (<! (exec exiftool))]
      (if (some? (:error exif))
        (assoc photo :error (:error exif))
        (try
          (merge photo (first (js->clj (.parse js/JSON exif))))
          (catch :default error (assoc photo :error error)))))))

(defn with-transformed-exif-keys
  ""
  [photo]
  (let [key-map {"CreateDate"       :created       "ExposureTime" :exposure
                 "ScaleFactor35efl" :efl-scale     "FocalLength"  :focal-length
                 "LensType"         :lens          "Aperture"     :aperture
                 "ISO"              :iso           "Model"        :model
                 "ImageWidth"       :width         "ImageHeight"  :height
                 "GPSPosition"      :gps-position  "GPSAltitude"  :gps-altitude
                 "SourceFile"       :file}
        transform-key (fn [kv]
                        (let [new-key (get key-map (first kv))]
                          [(if (nil? new-key) (first kv) new-key) (last kv)]))]
    (into {} (map transform-key (into [] photo)))))

(defn with-transformed-exif-values
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

(defn with-placeholders
  [photo]
  (cond-> photo
    (empty? (:lens photo)) (assoc :lens "unknown lens")
    (empty? (:model photo)) (assoc :model "unknown camera")))

(defn with-height-scale
  ""
  [photo]
  (assoc photo :height-scale (/ (:height photo) (:width photo))))

(defn with-href
  [prefix photo]
  (assoc photo :href (str prefix "/" (path-basename (:file photo)) (path-extension (:file photo)))))

(defn step
  ""
  [s]
  (let [chan? #(satisfies? cljs.core.async.impl.protocols/ReadPort %)]
    (fn [ch]
      (go
        (let [arg (<! ch)]
          (if (or (some? (:error arg)) (:metadata-cached arg))
            arg
            (let [return (s arg)] (if (chan? return) (<! return) return))))))))

(defn photo-processor
  ""
  [config]
  (comp (filter image?)
        (map (partial as-photo (:img-src-dir config)))
        (map with-timestamps)
        (map (step (partial resize-with-breakpoints! (:breakpoints config) (:img-out-dir config))))
        (map (step (partial link-original! (:img-out-dir config))))
        (map (step (partial with-cached-metadata (:metadata-cache config))))
        (map (step (partial with-sizes (:href-prefix config) (:breakpoints config))))
        (map (step (partial with-srcset (:href-prefix config) (:breakpoints config))))
        (map (step (partial with-exif (:exif-props config))))
        (map (step with-transformed-exif-keys))
        (map (step (partial with-transformed-exif-values (:custom-exif-transforms config))))
        (map (step with-placeholders))
        (map (step with-height-scale))
        (map (step (partial with-href (:href-prefix config))))))

(defn process-dir
  ""
  [dir-path the-machine]
  (go
    (let [return (chan 1 the-machine)
          files  (<! (read-dir dir-path))]
      (onto-chan return files)
      return)))

(defn error-photo
   ""
   [photo]
   (cond
     (some? (:error photo)) :errors
     :else :photos))

(defn process
  ""
  [config]
  (go
    (let [timestamp (timestamp-now)
          photos-ch (<! (process-dir (:img-src-dir config) (photo-processor config)))
          result    (group-by error-photo (loop [photo-ch (<! photos-ch)
                                                 accum    []]
                                            (if (some? photo-ch)
                                              (recur (<! photos-ch) (conj accum (<! photo-ch)))
                                              accum)))]
      (let [metadata (<! (write-metadata! (:metadata-format config)
                                          (:metadata-path config)
                                          (:photos result)
                                          (:html-tmpl config)))
            metadata-cache (<! (write-metadata-cache! (:img-src-dir config)
                                                      (generate-metadata-cache (:photos result)
                                                                               timestamp)))]
        {:wrote-metadata (nil? (:error metadata))
         :wrote-metadata-cache (nil? (:error metadata-cache))
         :count (count (:photos result))
         :cached-metadata (count (filter :cached-metadata (:photos result)))
         :fresh (filter #(nil? (:cached-metadata %)) (:photos result))
         :errors (:errors result)}))))
