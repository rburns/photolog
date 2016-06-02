(ns photolog.process.html
  (:require [clojure.string :refer [replace join]]
            [photolog.process.node-deps :refer [read-file-sync write-file-sync]]))

(defn as-html-image
  [image]
  (str "<div></div>"))

(defn write-html!
  [path data template]
  (let [template (read-file-sync template #js {"encoding" "utf-8"})]
    (write-file-sync path (replace template "##PHOTOS##" (join (map as-html-image data))))))
