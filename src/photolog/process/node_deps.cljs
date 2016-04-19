(ns photolog.process.node-deps
  (:require [cljs.nodejs :as node]))

(def readdir-sync (.-readdirSync (node/require "fs")))
(def write-file-sync (.-writeFileSync (node/require "fs")))

(def resolve-path (.-resolve (node/require "path")))
(def path-extension (.-extname (node/require "path")))

(def exec-sync (.-execSync (node/require "child_process")))

(def sharp (node/require "sharp"))
