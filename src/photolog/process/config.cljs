(ns photolog.process.config
  (:require [photolog.process.node-deps :refer [resolve-path file-exists-sync read-file-sync
                                                path-dirname path-basename]]))

(def defaults
  ""
  {:img-src-dir   nil,
   :img-out-dir   nil,
   :metadata-path nil,
   :href-prefix   nil,
   :exif-props    ["CreateDate" "ExposureTime" "ScaleFactor35efl" "FocalLength" "LensType"
                   "Aperture" "ISO" "Model" "ImageWidth" "ImageHeight"]
   :breakpoints   [[:tiny 200] [:small 556] [:medium 804] [:large 1000]]})

(defn handle-error
  [handler error]
  (handler error)
  nil)

(defn with-resolved-paths
  ""
  [config]
  (assoc config :img-src-dir (resolve-path (:img-src-dir config))
                :img-out-dir (resolve-path (:img-out-dir config))
                :metadata-path (resolve-path (:metadata-path config))
                :href-prefix (resolve-path (:href-prefix config))))

(defn parsed-config
  ""
  [config-path error-fn]
  (if (file-exists-sync config-path)
    (try (-> (.parse js/JSON (read-file-sync config-path))
             (js->clj :keywordize-keys true)
             with-resolved-paths)
         (catch js/Error e (handle-error error-fn (str config-path " is not valid JSON."))))
    (handle-error error-fn (str config-path " does not exist."))))

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

(defn config-with-defaults
  ""
  [config-path default-config error-fn]
  (-> config-path
      (parsed-config error-fn)
      (merged-config default-config)
      (valid-config error-fn)))
