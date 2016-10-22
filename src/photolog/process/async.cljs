(ns photolog.process.async
  (:require [cljs.core.async :as async :refer [chan <! >! close! onto-chan]]
            [photolog.process.node-deps :as cb])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn ->single-value-chan
  ""
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

(defn ch-in-out
  ""
  [transform returns-chan?]
  (fn [ch-in]
    (let [out-ch (chan)]
      (go (>! out-ch (transform (<! in-ch)))))))

(def file-stat (->single-value-chan cb/file-stat))
(def read-dir (->single-value-chan cb/read-dir))

; (defn photos-in-dir
;   ""
;   [photo-filter dir-path]
;   (let [
;         result-ch     (chan 5 (comp (filter photo-filter)
;                                     (map absolute-path)
;                                     (map with-stat)))]

;     (go (onto-chan result-ch (<! (read-dir dir-path))))
;     result-ch))


(photos-in-dir)

(defn init-photo
  ""
  [filename]
  {:absolute-path (str dir-path filename)})

(defn with-stat
  ""
  [photo]
  (go (assoc photo :stat (<! (file-stat (:absolute-path photo))))))

(def transform (comp (filter photo-filter)
                     (map init-photo)
                     (map with-stat)))

