(ns photolog.process.html
  (:require [clojure.string :refer [replace join split]]
            [photolog.process.node-deps :refer [read-file-sync write-file-sync path-basename]]))

(defn as-html-image
  [image]
  (str "<a class=\"photo\" id=\"" (first (split (path-basename (:file image)) ".")) "\""
          "href=\"" (:href image) "\" target=\"blank\">"
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
         "/>"
       "</a>"))

(defn write-html!
  [path data template]
  (let [template (read-file-sync template #js {"encoding" "utf-8"})]
    (write-file-sync path (replace template "##PHOTOS##" (join (map as-html-image data))))))
