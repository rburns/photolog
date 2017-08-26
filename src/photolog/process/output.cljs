(ns photolog.process.output
  (:require [clojure.string :refer [replace join split]]
            [cognitect.transit :as transit]
            [photolog.platform-node :refer [read-file-sync write-file path-basename create-feed
                                            feed-add-item ->feed-date]]))

(defn ->transit
  ""
  [data]
  (transit/write (transit/writer :json) data))

(defn as-html-image
  ""
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
  ""
  [data template]
  (replace (read-file-sync template #js {"encoding" "utf-8"})
           "##PHOTOS##"
           (join (map as-html-image data))))

(defn atom-item-content
  ""
  [image]
  (str "<img src=\"" (-> image :sizes first :href) "\" />"))

(defn ->atom
  ""
  [data]
  (let [feed (create-feed {})]
    (doseq [image data]
      (feed-add-item feed {:date (->feed-date (:created image))
                           :link (:href image)
                           :content (atom-item-content image)
                           :id (:href image)}))
    (.atom1 feed)))

(defn write-metadata!
  ""
  [format path data template]
  (write-file path (condp = format
                    :transit (->transit data)
                    :html    (->html data template)
                    :atom    (->atom data))))
