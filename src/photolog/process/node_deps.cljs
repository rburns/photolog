(ns photolog.process.node-deps
  (:require [cljs.nodejs :as node]))

(def write-file-sync (.-writeFileSync (node/require "fs")))
(def exec-sync (.-execSync (node/require "child_process")))
(def sharp (node/require "sharp"))

(def resolve-path (.-resolve (node/require "path")))
(def path-extension (.-extname (node/require "path")))
(def path-basename (let [basename (.-basename (node/require "path"))]
                     (fn [path] (basename path (path-extension path)))))

(def write-stdout (let [stdout (.-stdout (node/require "process"))]
                    (fn [output] (.write stdout output))))
