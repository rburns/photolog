(ns photolog.process.node-deps
  (:require [cljs.nodejs :as node]))

(def exec-sync (.-execSync (node/require "child_process")))
(def write-file-sync (.-writeFileSync (node/require "fs")))
