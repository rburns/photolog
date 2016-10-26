(ns photolog.process.core-test
  (:require [cljs.test :refer-macros [deftest is]]
            [photolog.process.core :as p]))

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
