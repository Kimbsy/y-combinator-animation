(ns y-combinator-animation.common
  (:require [quip.tween :as qptween]))

(def grey [57 57 58])
(def green [178 255 99 255])
(def slide-bg-blue [45 55 67])
(def blue [144 180 253 255])
(def orange [245 143 41 255])
(def white [230 230 230 255])

;; @TODO: push these version of darken/lighten up to quip, they
;; preserve the colour being represented as a vector which is required
;; for tweening the component colors.
(defn darken
  "Darken a colour by 30, preserving alpha component if present."
  [[r g b a :as color]]
  (if a
    (assoc (mapv #(max 0 (- % 30)) color) 3 a)
    (mapv #(max 0 (- % 30)) color)))

(defn lighten
  "Lighten a colour by 30, preserving alpha component if present."
  [[r g b a :as color]]
  (if a
    (assoc (mapv #(min 255 (+ % 30)) color) 3 a)
    (mapv #(min 255 (+ % 30)) color)))

;; @TODO: push this back up to quip.
(defn tween-to-color
  ([s c]
   (tween-to-color s c {}))
  ([{sprite-color :color :as sprite} target-color opts]
   (let [component-deltas (map - target-color sprite-color)]
     (first
      (reduce (fn [[s i] cd]
                (let [update-fn (fn [c d] (update c i + d))
                      yoyo-update-fn (fn [c d] (update c i - d))]
                  [(qptween/add-tween
                    s
                    (qptween/tween
                     :color
                     cd
                     :update-fn (fn [c d] (update c i + d))
                     (if (:yoyo? opts)
                       (assoc opts :yoyo-update-fn yoyo-update-fn)
                       opts)))
                   (inc i)]))
              [sprite 0]
              component-deltas)))))


;; @TODO: push this to quip so we can make multi-group preds
;; easily. Maybe switch on whether the arg is a seq? then we can have
;; both in one?
(defn groups-pred
  "Defines a predicate that filters sprites based on their
  sprite-group.

  Commonly used alongside `update-sprites-by-pred`:

  (qpsprite/update-sprites-by-pred
    state
    (qpsprite/groups-pred [:asteroids :black-holes])
    sprite-update-fn)"
  [sprite-groups]
  (fn [s]
    ((set sprite-groups) (:sprite-group s))))
