(ns cljs-samples.core
  (:require [cljs.core.async :refer [chan put! take! timeout sliding-buffer] :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def colors ["#FF0000"
             "#FF0000"
             "#FF0000"
             "#FF0000"
             ])

(def channel (chan (sliding-buffer 10000)))

(def context (-> js/document
                 (.getElementById "canvas")
                 (.getContext "2d")))

(def pixel-size
  10)

(defn- inc-color
  [x]
  (if (= x 63)
    0
    (inc x)))

(defn to-color
  [r g b]
  (str "#" (.toString r 16) (.toString g 16) (.toString b 16)))

(defn make-cell
  [x y]
  (set! (.-fillStyle context) (rand-nth colors))
  (.fillRect context x y pixel-size pixel-size)
  (go (loop [r 0 g 0 b 0]
        (>! channel [x y (to-color r g b)])
        (<! (timeout (rand-int 1000)))
        (recur (inc-color r) (inc-color g) (inc-color b)))))

(defn read-cell-data
  []
  (go (loop [[x y c] (<! channel)]
        (set! (.-fillStyle context) c)
        (.fillRect context x y pixel-size pixel-size)
        (recur (<! channel)))))

(defn make-scene [rows cols]
  (dotimes [x cols]
    (dotimes [y rows]
      (make-cell (* pixel-size x) (* pixel-size y)))))

(make-scene 100 100)

(read-cell-data)
