(ns photolog.process.config
  (:require [photolog.platform-node :refer [resolve-path file-exists-sync read-file-sync
                                            path-dirname path-basename]]
            [photolog.process.metadata-cache :refer [metadata-cache]]))

(def defaults
  ""
  {:img-src-dir     nil,
   :img-out-dir     nil,
   :metadata-path   nil,
   :href-prefix     nil,
   :exif-props      ["CreateDate" "ExposureTime" "ScaleFactor35efl" "FocalLength" "LensType"
                     "Aperture" "ISO" "Model" "ImageWidth" "ImageHeight"]
   :breakpoints     [[:tiny 200] [:small 556] [:medium 804] [:large 1000]]
   :metadata-format :transit})

(defn handle-error
  [handler error]
  (handler error)
  nil)

(defn with-keyword-names
  ""
  [breakpoints]
  (map #(vector (keyword (first %)) (last %)) breakpoints))

(defn with-keywordized-values
  ""
  [config]
  (cond-> config
    (:metadata-format config) (assoc :metadata-format (keyword (:metadata-format config)))
    (:breakpoints config) (assoc :breakpoints (with-keyword-names (:breakpoints config)))))

(defn with-resolved-paths
  ""
  [config]
  (assoc config :img-src-dir (resolve-path (:img-src-dir config))
                :img-out-dir (resolve-path (:img-out-dir config))
                :metadata-path (resolve-path (:metadata-path config))
                :href-prefix (resolve-path (:href-prefix config))))

(defn parsed-config
  ""
  [config error-fn]
  (try (-> (.parse js/JSON config)
           (js->clj :keywordize-keys true)
           with-keywordized-values
           with-resolved-paths)
       (catch :default error (handle-error error-fn "config is not valid JSON."))))


(defn merged-config
  ""
  [config default-config]
  (if (some? config) (merge default-config config) nil))

(defn valid-config
  ""
  [config error-fn]
  (cond
    (nil? config)
    nil
    (not (file-exists-sync (:img-src-dir config)))
    (handle-error error-fn "img-src-dir must specify a valid directory path.")
    (not (file-exists-sync (:img-out-dir config)))
    (handle-error error-fn "img-out-dir must specify a valid directory path.")
    (not (file-exists-sync (path-dirname (:metadata-path config))))
    (handle-error error-fn "metadata-path must refer to a valid directory.")
    (nil? (path-basename (:metadata-path config)))
    (handle-error error-fn "metadata-path must include a file name.")
    (nil? (:href-prefix config))
    (handle-error error-fn "href-prefix must be specified")
    :else config))

(defn with-metadata-cache
  ""
  [config error-fn]
  (cond
    (nil? config) nil
    :else (assoc config :metadata-cache (metadata-cache (:img-src-dir config)))))

(defn config-with-defaults
  ""
  [config-path default-config error-fn]
  (if (file-exists-sync config-path)
    (-> (read-file-sync config-path)
        (parsed-config error-fn)
        (merged-config default-config)
        (valid-config error-fn)
        (with-metadata-cache error-fn))
    (handle-error error-fn (str config-path " does not exist."))))
