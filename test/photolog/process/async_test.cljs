(ns photolog.process.async-test
  (:require [cljs.test :refer-macros [deftest is async testing]]
            [cljs.core.async :as async :refer [<!]]
            [photolog.process.async :as a])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(testing "->single-value-chan"

  ;; things it doesn't do
  ;;
  ;; handle async functions with callback which doesn't follow the error, result convention
  ;; handle async function which take 0 arguments, apart from the callback

  (deftest async-function-returns-a-channel
    (async done
           (let [echo-cb   (fn [a cb] (cb nil a))
                 echo-chan (a/->single-value-chan echo-cb)]
             (go (is (= 1 (<! (echo-chan 1))))
                 (done)))))

  (deftest variable-arity-async-function
    (async done
           (let [echo-cb   (fn [a b c cb] (cb nil [a b c]))
                 echo-chan (a/->single-value-chan echo-cb)]
             (go (is (= [1 2 3] (<! (echo-chan 1 2 3))))
                 (done)))))

  (deftest propagates-errors
    (async done
           (let [error-cb   (fn [a cb] (cb "error" nil))
                 error-chan (a/->single-value-chan error-cb)]
             (go (is (= {:error "error"} (<! (error-chan 1))))
                 (done)))))

  (deftest propagates-exceptions
    (async done
           (let [error-cb   (fn [a cb] (throw "error"))
                 error-chan (a/->single-value-chan error-cb)]
             (go (is (= {:error "error"} (<! (error-chan 1))))
                 (done))))))
