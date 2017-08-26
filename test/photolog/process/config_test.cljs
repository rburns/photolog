(ns photolog.process.config-test
  (:require [cljs.test :refer-macros [deftest is]]
            [photolog.process.config :as c]))

(def minimal-config
  "{
    \"img-src-dir\": \"/img/src/dir\",
    \"img-out-dir\": \"/img/out/dir\",
    \"metadata-path\": \"/path/to/metadata\",
    \"href-prefix\": \"href_prefix\"
  }")

(def sample-config
  "{
    \"img-src-dir\": \"/img/src/dir\",
    \"img-out-dir\": \"/img/out/dir\",
    \"metadata-path\": \"/path/to/metadata\",
    \"href-prefix\": \"href_prefix\",
    \"metadata-format\": \"transit\",
    \"breakpoints\": [[\"tiny\", 100], [\"big\", 300]]
  }")

(def exif-override-config
  "{
    \"img-src-dir\": \"/img/src/dir\",
    \"img-out-dir\": \"/img/out/dir\",
    \"metadata-path\": \"/path/to/metadata\",
    \"exif-props\": [\"GPSPosition\", \"GPSAltitude\"],
    \"href-prefix\": \"href_prefix\",
    \"metadata-format\": \"transit\",
    \"breakpoints\": [[\"tiny\", 100], [\"big\", 300]]
  }")

(deftest top-level-keys-are-keywords
  (is (every? keyword? (keys (c/parsed-config sample-config #())))))

(deftest metatdata-format-value-is-keyword
  (is (keyword? (:metadata-format (c/parsed-config sample-config #())))))

(deftest breakpoint-names-are-keywords
  (is (= 2 (count (:breakpoints (c/parsed-config sample-config #())))))
  (is (every? keyword? (map first (:breakpoints (c/parsed-config sample-config #()))))))

(deftest parsed-config-adds-no-keys
  (is (= 4 (count (keys (c/parsed-config minimal-config #()))))))

(deftest resolve-href-prefix-absolute-path
  (is (= "/foo/bar"
         (c/resolve-href-prefix "/foo/bar"))))

(deftest resolve-href-prefix-absolute-url
  (is (= "http://foo/bar"
         (c/resolve-href-prefix "http://foo/bar"))))

(deftest default-exif-props
  (is (= (:exif-props c/defaults)
         (:exif-props (c/merged-config (c/parsed-config minimal-config) c/defaults)))))

(deftest override-exif-props
  (is (= ["GPSPosition" "GPSAltitude"]
         (:exif-props (c/merged-config (c/parsed-config exif-override-config) c/defaults)))))
