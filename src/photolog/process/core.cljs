(ns photolog.process.core
  (:require [clojure.string :refer [join]]
            [cljs.core.async :as async :refer [chan onto-chan <!]]
            [photolog.process.platform-node :refer [resolve-path sharp write-file-sync stat-path
                                                    path-basename path-extension write-stdout exec
                                                    timestamps file-exists-error? link-path
                                                    read-dir]]
            [photolog.process.output :refer [write-output!]])
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

(defn output-file
  ""
  [source-path label]
  (str (path-basename source-path)
       (when label (str "-" (name label)))
       (path-extension source-path)))

(defn output-path
  [output-dir source-path label]
  (str output-dir "/" (output-file source-path label)))

; (defn resize
;   ""
;   [source-path output-dir breakpoint]
;   (let [label        (first breakpoint)
;         width        (.floor js/Math (last breakpoint))]
;     (try
;       (-> (sharp source-path)
;           (.resize width nil)
;           (.quality 95)
;           (.withoutChromaSubsampling)
;           (.toFile (output-path output-dir source-path label) print-feedback))
;       (catch :default error (print-feedback error nil)))))

; (defn resize-with-breakpoints!
;   ""
;   [breakpoints output-dir photo]
;   (doseq [breakpoint breakpoints] (resize (:file photo) output-dir breakpoint))
;   photo)

(defn link-original!
  [output-dir photo]
  (go
    (let [result (<! (link-path (:file photo) (output-path output-dir (:file photo) nil)))]
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
  [cache props photo]
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
  (let [key-map {"CreateDate"       :created   "ExposureTime" :exposure
                 "ScaleFactor35efl" :efl-scale "FocalLength"  :focal-length
                 "LensType"         :lens      "Aperture"     :aperture
                 "ISO"              :iso       "Model"        :model
                 "ImageWidth"       :width     "ImageHeight"  :height
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
          (if (some? (:error arg)) arg (let [return (s arg)]
                                         (if (chan? return) (<! return) return))))))))

(defn photo-processor
  ""
  [config]
  (comp (filter image?)
        (map (partial as-photo (:img-src-dir config)))
        (map with-timestamps)
        ; (map (step (partial resize-with-breakpoints! (:breakpoints config) (:img-out-dir config))))
        (map (step (partial link-original! (:img-out-dir config))))
        (map (step (partial with-sizes (:href-prefix config) (:breakpoints config))))
        (map (step (partial with-srcset (:href-prefix config) (:breakpoints config))))
        (map (step (partial with-exif (:exif-cache config) (:exif-props config))))
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
    (let [photos-ch (<! (process-dir (:img-src-dir config) (photo-processor config)))
          result    (group-by error-photo (loop [photo-ch (<! photos-ch)
                                                 accum    []]
                                            (if (some? photo-ch)
                                              (recur (<! photos-ch) (conj accum (<! photo-ch)))
                                              accum)))]
      (write-output! (:metadata-format config)
                     (:metadata-path config)
                     (:photos result)
                     (:html-tmpl config))
      {:count (count (:photos result))
       :errors (:errors result)})))
