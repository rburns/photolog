(ns photolog.process.async
  (:require [cljs.core.async :as async :refer [chan >! close!]]
            [photolog.process.platform-node :as cb])
  (:require-macros [cljs.core.async.macros :refer [go]]))

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

(def stat-path (->single-value-chan cb/stat-path))
(def read-dir (->single-value-chan cb/read-dir))
(def exec (->single-value-chan cb/exec))
(def link-path (->single-value-chan cb/link-path))
