(ns photolog.annotate.core
  (:require [photolog.platform-node :refer [file-exists-sync path-dirname]]))

(defn valid-command
  ""
  [command error-fn]
  (let [prop?     (fn [prop] (contains? #{:section :caption} prop))
        commands  {:w 2
                   :d 1
                   :m 2
                   :l 0}
        error     (fn [err] (error-fn err) nil)]
   (cond
     (nil? command)
     nil

     (nil? (:photo command))
     (error "You must supply a photo.")

     (not (file-exists-sync (:photo command)))
     (error (str (:photo command) " does not exist."))

     (nil? (:command command))
     (error "You must supply a command.")

     (not (contains? (into #{} (keys commands)) (:command command)))
     (error (str (name (:command command)) " is not a valid command."))

     (and (= :w (:command command)) (> ((:command command) commands) (count (:params command))))
     (error (str "The w command requires a property and a value."))

     (and (= :w (:command command)) (not (prop? (first (:params command)))))
     (error (str (name (first (:params command))) " is not a valid property."))

     (and (= :w (:command command)) (not (string? (second (:params command)))))
     (error (str (second (:params command)) " is not a string"))

     (and (= :d (:command command)) (> ((:command command) commands) (count (:params command))))
     (error (str "Delete requires a property."))

     (and (= :d (:command command)) (not (prop? (first (:params command)))))
     (error (str (name (first (:params command))) " is not a valid property."))

     (and (= :m (:command command)) (> ((:command command) commands) (count (:params command))))
     (error (str "Move requires a property and a destination photo."))

     (and (= :m (:command command)) (not (prop? (first (:params command)))))
     (error (str (name (first (:params command))) " is not a valid property."))

     (and (= :m (:command command)) (not (file-exists-sync (second (:params command)))))
     (error (str (second (:params command)) " does not exist."))

     :else command)))
