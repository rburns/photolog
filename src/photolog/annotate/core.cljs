(ns photolog.annotate.core
  (:require [photolog.platform-node :refer [file-exists-sync path-dirname]]))

(defn handle-error
  [handler error]
  (handler error)
  nil)

(defn valid-command
  ""
  [command error-fn]
  (let [prop?     (fn [prop] (contains? #{:section :caption} prop))
        commands  {:w [prop? some?]
                   :d [prop?]
                   :m [prop? file-exists-sync]
                   :l []}
        error     (partial handle-error error-fn)]
   (cond
     (nil? command)
     nil
     (not (file-exists-sync (:photo command)))
     (error (str (:photo command) " does not exist."))
     :else command)))
