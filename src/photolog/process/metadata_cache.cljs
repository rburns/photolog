(ns photolog.process.metadata-cache
  (:require [cljs.core.async :as async :refer [chan <!]]
            [cognitect.transit :as transit]
            [photolog.process.platform-node :refer [file-exists-sync read-file-sync path-basename
                                                    path-extension write-file]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def metadata-cache-filename ".photolog-metadata.cache")

(defn gen-key
  ""
  [filename]
  (str (path-basename filename) (path-extension filename)))

(defn metadata-cache
  ""
  [cache-dir]
  (let [cache-path (str cache-dir "/" metadata-cache-filename)
        cache-store (if (file-exists-sync cache-path)
                      (transit/read (transit/reader :json) (read-file-sync cache-path))
                      {})]
    (fn [filename modified]
      (let [cache-key (gen-key filename)
            cached (get cache-store cache-key)]
        (if (and (some? cached) (<= modified (:metadata_cached cached)))
          cached
          false)))))

(defn generate-metadata-cache
  [photos timestamp]
  (reduce (fn [cache photo]
            (assoc cache
                   (gen-key (:file photo))
                   (-> photo
                       (assoc :metadata-cached (or (:metadata-cached photo) timestamp))
                       (dissoc :file :cached-metadata))))
          {}
          photos))

(defn write-metadata-cache!
  [cache-dir data]
  (write-file (str cache-dir "/" metadata-cache-filename)
              (transit/write (transit/writer :json) data)))
