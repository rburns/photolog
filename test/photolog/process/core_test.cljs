(ns photolog.process.core-test
  (:require [cljs.test :refer-macros [deftest is]]
            [photolog.process.core :as p]))

(deftest jpg-image?
  (is (p/image? "/foo/bar.jpg"))
  (is (p/image? "foo/bar.jpg"))
  (is (p/image? "/foo/bar.JPG"))
  (is (p/image? "/foo/bar.jpeg"))
  (is (p/image? "/foo/bar.JPG"))
  (is (p/image? "bar.JPG"))
  (is (p/image? "bar.png"))
  (is (p/image? "bar.PNG"))
  (is (p/image? "/bar.tiff"))
  (is (p/image? "bar.tif"))
  (is (p/image? "foo/bar.TIFF"))
  (is (p/image? "bar.TIF")))

(deftest empty-transforms
  (is (= {:foo "bar"}
         (p/with-transformed-exif-values [] {:foo "bar"}))))

(deftest non-applicable-transform
  (is (= {:foo "bar"}
         (p/with-transformed-exif-values [[:foo "a" "b"]] {:foo "bar"}))))

(deftest applicable-transform
  (is (= {:foo "b"}
         (p/with-transformed-exif-values [[:foo "a" "b"]] {:foo "a"}))))

(deftest applicable-transform-of-many
  (is (= {:foo "b"}
         (p/with-transformed-exif-values [[:foo "a" "b"] [:foo "c" "d"]] {:foo "a"}))))

(deftest many-applicable-transforms
  (is (= {:foo "b", :bar "d"}
         (p/with-transformed-exif-values [[:foo "a" "b"] [:bar "c" "d"]] {:foo "a", :bar "c"}))))

(deftest with-href-absolute-path-prefix
  (is (= "/foo/photo.jpg"
         (:href (p/with-href "/foo" {:file "/on/disk/path/to/photo.jpg"})))))

(deftest with-href-absoulte-url-prefix
  (is (= "http://foo.com/images/photo.jpg"
         (:href (p/with-href "http://foo.com/images" {:file "/on/disk/path/to/photo.jpg"})))))

(deftest transform-exif-created-prop
  (is (= {:created 0}
         (p/with-transformed-exif-keys {"CreateDate" 0}))))

(deftest transform-exif-exposure-prop
  (is (= {:exposure 0}
         (p/with-transformed-exif-keys {"ExposureTime" 0}))))

(deftest transform-exif-efl-scale-prop
  (is (= {:efl-scale 0}
         (p/with-transformed-exif-keys {"ScaleFactor35efl" 0}))))

(deftest transform-exif-focal-length-prop
  (is (= {:focal-length 0}
         (p/with-transformed-exif-keys {"FocalLength" 0}))))

(deftest transform-exif-lens-prop
  (is (= {:lens 0}
         (p/with-transformed-exif-keys {"LensType" 0}))))

(deftest transform-exif-aperture-prop
  (is (= {:aperture 0}
         (p/with-transformed-exif-keys {"Aperture" 0}))))

(deftest transform-exif-ISO-prop
  (is (= {:iso 0}
         (p/with-transformed-exif-keys {"ISO" 0}))))

(deftest transform-exif-model-prop
  (is (= {:model 0}
         (p/with-transformed-exif-keys {"Model" 0}))))

(deftest transform-exif-width-prop
  (is (= {:width 0}
         (p/with-transformed-exif-keys {"ImageWidth" 0}))))

(deftest transform-exif-height-prop
  (is (= {:height 0}
         (p/with-transformed-exif-keys {"ImageHeight" 0}))))

(deftest transform-exif-gps-position-prop
  (is (= {:gps-position 0}
         (p/with-transformed-exif-keys {"GPSPosition" 0}))))

(deftest transform-exif-gps-altitude-prop
  (is (= {:gps-altitude 0}
         (p/with-transformed-exif-keys {"GPSAltitude" 0}))))

(deftest transform-exif-file-prop
  (is (= {:file 0}
         (p/with-transformed-exif-keys {"SourceFile" 0}))))
