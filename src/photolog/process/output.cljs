(ns photolog.process.output
  (:require [clojure.string :refer [replace join split]]
            [cognitect.transit :as transit]
            [photolog.process.platform-node :refer [read-file-sync write-file-sync path-basename]]))

(defn write-transit!
  ""
  [path data]
  (write-file-sync path (transit/write (transit/writer :json) data)))

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

(defn write-html!
  [path data template]
  (let [template (read-file-sync template #js {"encoding" "utf-8"})]
    (write-file-sync path (replace template "##PHOTOS##" (join (map as-html-image data))))))

(defn write-output!
  [format path data template]
  (condp = format
    :transit (write-transit! path data)
    :html   (write-html! path data template)))

