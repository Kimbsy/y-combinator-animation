(ns y-combinator-animation.common
  (:require [quip.tween :as qptween]))

(def grey [57 57 58])
(def green [178 255 99 255])
(def blue [144 180 253 255])
(def orange [245 143 41 255])
(def white [230 230 230 255])

;; @TODO: push this back up to quip.
(defn tween-to-color
  ([s c]
   (tween-to-color s c {}))
  ([{sprite-color :color :as sprite} target-color opts]
   (let [component-deltas (map - target-color sprite-color)]
     (first
      (reduce (fn [[s i] cd]
                [(qptween/add-tween
                  s
                  (qptween/tween
                   :color
                   cd
                   :update-fn (fn [c d] (update c i + d))
                   opts))
                 (inc i)])
              [sprite 0]
              component-deltas)))))
