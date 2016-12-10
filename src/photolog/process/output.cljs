(ns photolog.process.output
  (:require [clojure.string :refer [replace join split]]
            [cognitect.transit :as transit]
            [photolog.process.platform-node :refer [read-file-sync write-file path-basename]]))

(defn ->transit
  ""
  [data]
  (transit/write (transit/writer :json) data))

(defn as-html-image
  [image]
  (str "<div class=\"photo\" id=\"" (first (split (path-basename (:file image)) ".")) "\">"
         "<img srcset=\"" (:srcset image) "\" "
              "data-created=\"" (:created image) "\" "
              "data-aperture=\"" (:aperture image) "\" "
              "data-exposure=\"" (:exposure image) "\" "
              "data-iso=\"" (:iso image) "\" "
              "data-focal-length=\"" (:focal-length image) "\" "
              "data-lens=\"" (:lens image) "\" "
              "data-model=\"" (:model image) "\" "
              "data-efl-scale\"" (:efl-scale image) "\" "
              "data-height=\"" (:height image) "\" "
              "data-width=\"" (:width image) "\" "
              "data-height-scale=\"" (:height-scale image) "\" "
              "data-sizes='" (.stringify js/JSON (clj->js (:sizes image))) "' "
         "/>"
       "</div>"))

(defn ->html
  [data template]
  (replace (read-file-sync template #js {"encoding" "utf-8"})
           "##PHOTOS##"
           (join (map as-html-image data))))

(defn write-metadata!
  [format path data template]
  (write-file path (condp = format
                    :transit (->transit data)
                    :html   (->html data template))))
