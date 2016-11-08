(ns photolog.platform-node
  (:require [cljs.nodejs :as node]
            [cljs.core.async :as async :refer [chan <! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn timestamp-now [] (.now js/Date))

(def write-file-sync (.-writeFileSync (node/require "fs")))
(def read-file-sync (.-readFileSync (node/require "fs")))
(def file-exists-sync (.-existsSync (node/require "fs")))

(def resolve-path (let [resolve (.-resolve (node/require "path"))]
                    (fn [path] (when (string? path) (resolve path)))))
(def path-extension (.-extname (node/require "path")))
(def path-dirname (.-dirname (node/require "path")))
(def path-basename (let [basename (.-basename (node/require "path"))]
                     (fn [path] (basename path (path-extension path)))))

(def set-env (let [env (.-env (node/require "process"))]
               (fn [env-var env-var-val] (aset env env-var env-var-val))))

(def process-argv (let [argv (.-argv (node/require "process"))]
                    (fn [] (js->clj argv))))

(def write-stdout (let [stdout (.-stdout (node/require "process"))]
                    (fn [output] (.write stdout output))))

(defn ->single-value-chan
  ""
  [async-fn]
  (fn [args &] (chan)
    (let [result-chan (chan)
          callback    (fn [error result]
                        (go (>! result-chan (if (some? error) {:error error} (or result {})))
                            (close! result-chan)))]
      (try (apply async-fn (concat args [callback]))
           (catch :default error
             (go (>! result-chan {:error error})
                 (close! result-chan))))
      result-chan)))

(def stat-path (->single-value-chan (.-stat (node/require "fs"))))

(def read-dir (->single-value-chan (.-readdir (node/require "fs"))))

(def exec (->single-value-chan (.-exec (node/require "child_process"))))

(def symlink-path (->single-value-chan (.-symlink (node/require "fs"))))

(def write-file (->single-value-chan (.-writeFile (node/require "fs"))))

(defn timestamps
  [stat]
  {:file-created (.getTime (.-birthtime stat))
   :file-modified (.getTime (.-mtime stat))})

(defn file-exists-error?
  [err]
  (and (some? err) (= "EEXIST" (.-code err))))

(defn file-does-not-exist-error?
  [err]
  (and (some? err) (= "ENOENT" (.-code err))))

(def sharp (node/require "sharp"))

(defn sharp-resize
  ""
  [source-path output-path breakpoint next]
  (let [label        (first breakpoint)
        width        (.floor js/Math (last breakpoint))]
    (try
      (-> (sharp source-path)
          (.resize width nil)
          (.quality 95)
          (.withoutChromaSubsampling)
          (.toFile output-path next))
      (catch :default error (next error nil)))))

(def resize (->single-value-chan sharp-resize))
