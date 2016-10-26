(ns photolog.process.platform-node
  (:require [cljs.nodejs :as node]))

(def stat-path (.-stat (node/require "fs")))

(defn timestamps
  [stat]
  {:file-created (.getTime (.-birthtime stat))
   :file-modified (.getTime (.-mtime stat))})

(def read-dir (.-readdir (node/require "fs")))

(def link-path (.-link (node/require "fs")))

(defn file-exists-error?
  [err]
  (= "EEXIST" (.-code err)))

(def write-file-sync (.-writeFileSync (node/require "fs")))
(def read-file-sync (.-readFileSync (node/require "fs")))
(def file-exists-sync (.-existsSync (node/require "fs")))

(def resolve-path (let [resolve (.-resolve (node/require "path"))]
                    (fn [path] (when (string? path) (resolve path)))))
(def path-extension (.-extname (node/require "path")))
(def path-dirname (.-dirname (node/require "path")))
(def path-basename (let [basename (.-basename (node/require "path"))]
                     (fn [path] (basename path (path-extension path)))))

(def process-argv (.-argv (node/require "process")))
(def write-stdout (let [stdout (.-stdout (node/require "process"))]
                    (fn [output] (.write stdout output))))

(def exec (.-exec (node/require "child_process")))

(def sharp (node/require "sharp"))
