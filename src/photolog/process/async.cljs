(ns photolog.process.async
  (:require [cljs.core.async :refer [chan >! close!]]
            [photolog.process.node-deps :refer [file-stat]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn ->single-value-chan
  [async-fn]
  (fn [args &] (chan)
    (let [result-chan (chan)
          callback    (fn [error result]
                        (go (>! result-chan (if (some? error) {:error error} result))
                            (close! result-chan)))]
      (try (apply async-fn (concat args [callback]))
           (catch :default error
             (go (>! result-chan {:error error})
                 (close! result-chan))))
      result-chan)))
