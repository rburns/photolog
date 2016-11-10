(ns photolog.annotate.core-test
  (:require [cljs.test :refer-macros [deftest is async testing]]
            [photolog.annotate.core :as a]))

(def test-photo-path "/home/rburns/projects/photolog/test/test-photo.png")

(defn test-invalid
  [command error-pattern done]
  (is (= nil (a/valid-command command #())))
  (a/valid-command command (fn [err]
                             (is (some? (re-matches error-pattern err)))
                             (done))))

(deftest no-photo
  (async done (test-invalid {} #".*supply.+photo.*" done)))

(deftest non-existing-photo
  (async done (test-invalid {:photo "/no/file/here.jpg"} #".*does not exist.*" done)))

(deftest no-command
  (async done (test-invalid {:photo test-photo-path} #".*supply.+command.*" done)))

(deftest invalid-command
  (async done (test-invalid {:photo test-photo-path
                             :command "z"} #".*not.*command.*" done)))

(deftest write-nil-params
  (async done (test-invalid {:photo test-photo-path
                             :command :w} #".*requires.*property.*value.*" done)))

(deftest write-zero-params
  (async done (test-invalid {:photo test-photo-path
                             :command :w
                             :params []} #".*requires.*property.*value.*" done)))

(deftest write-insufficient-params
  (async done (test-invalid {:photo test-photo-path
                             :command :w
                             :params ["x"]} #".*requires.*property.*value.*" done)))

(deftest write-invalid-property
  (async done (test-invalid {:photo test-photo-path
                             :command :w
                             :params ["x" "y"]} #".*not.*valid.*property.*" done)))

(deftest write-invalid-value
  (async done (test-invalid {:photo test-photo-path
                             :command :w
                             :params [:caption 34]} #".*not.*string.*" done)))

(deftest delete-nil-params
  (async done (test-invalid {:photo test-photo-path
                             :command :d} #".*requires.*property.*" done)))

(deftest delete-zero-params
  (async done (test-invalid {:photo test-photo-path
                             :command :d
                             :params []} #".*requires.*property.*" done)))

(deftest delete-invalid-property
  (async done (test-invalid {:photo test-photo-path
                             :command :d
                             :params [:foo]} #".*not.*valid.*property.*" done)))

(deftest move-nil-params
  (async done (test-invalid {:photo test-photo-path
                             :command :m} #".*requires.*property.*destination.*" done)))

(deftest move-zero-params
  (async done (test-invalid {:photo test-photo-path
                             :command :m
                             :params []} #".*requires.*property.*destination.*" done)))

(deftest move-insufficient-params
  (async done (test-invalid {:photo test-photo-path
                             :command :m
                             :params [:bar]} #".*requires.*property.*destination.*" done)))

(deftest move-invalid-property
  (async done (test-invalid {:photo test-photo-path
                             :command :m
                             :params [:qux "y"]} #".*not.*valid.*property.*" done)))

(deftest move-invalid-destination
  (async done (test-invalid {:photo test-photo-path
                             :command :m
                             :params [:caption "/foo.jpg"]} #".*does.*not.*exist.*" done)))
