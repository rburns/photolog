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

(deftest top-level-keys-are-keywords
  (is (every? keyword? (keys (c/parsed-config sample-config #())))))

(deftest metatdata-format-value-is-keyword
  (is (keyword? (:metadata-format (c/parsed-config sample-config #())))))

(deftest breakpoint-names-are-keywords
  (is (= 2 (count (:breakpoints (c/parsed-config sample-config #())))))
  (is (every? keyword? (map first (:breakpoints (c/parsed-config sample-config #()))))))

(deftest parsed-config-adds-no-keys
  (is (= 4 (count (keys (c/parsed-config minimal-config #()))))))
