(ns y-combinator-animation.scenes.circles
  (:require [quil.core :as q]
            [quip.delay :as qpdelay]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]
            [clojure.string :as s]
            [y-combinator-animation.common :as common]))

(def text-size (* 4 qpu/default-text-size))

;; magic variable for pleasant vertical line spacing
(def dy 1.3)

;; show self evl lambda, show circle evaluating to itself in a loop

;; show wrappped, show circle wrapping circle wrapping circle expanding outward

;; show delayed, then do rest?







;; This animation is meant to help explain the process of dynamically building the chain of functions sing the delayed evaluation lambda.

;; Each function has a reference to a function that when invoked will create the next step of the iteration.

;; Show (Y f), it should create a circle representing this iteration.

;; The circle should contain a `recur-fn` thing

;; the circle should show a condition being checked, then invoking the `recur-fn` which creates a new circle

;; then again

;; then again


;; @NOTE: do we want this?
;; then condition should return false, return value?

(defn draw-self-circle
  [{[x y :as pos] :pos
    :keys [color stroke-color size]
    :as circle}]
  (when (pos? size)
    (qpu/fill color)
    (qpu/stroke stroke-color)
    (q/stroke-weight 6)
    (q/ellipse x y size size)))

(defn draw-wrapped-circle
  [{[x y :as pos] :pos
    :keys [color stroke-color size]
    :as circle}]
  (doseq [i (range 200)]
    (let [inner-size (- size (* i 300))]
      (when (and (pos? inner-size)
                 (< inner-size (+ (q/width) (q/height))))
        (qpu/fill color)
        (qpu/stroke stroke-color)
        (q/stroke-weight 6)
        (q/ellipse x y inner-size inner-size)))))

(defn draw-delayed-circle
  [s]
  ;; @TODO: replace
  (draw-self-circle s))

(defn circle
  [sprite-group pos size]
  {:sprite-group sprite-group
   :draw-fn (sprite-group {:self draw-self-circle
                           :wrapped draw-wrapped-circle
                           :delayed draw-delayed-circle})
   :update-fn identity
   :pos pos
   :color common/blue
   :stroke-color common/white
   :size size})


;; @TODO: add text sprites showing which expression we're evaluating

(defn multi-line-text
  [content pos]
  (map-indexed
   (fn [i s]
     (qpsprite/text-sprite
      s
      (map + pos [0 (* dy i text-size)])
      :color common/white
      :size text-size
      :offsets [:left :top]))
   (clojure.string/split content #"\n")))

(defn self-sprites
  []
  (concat
   [(circle :self [(* (q/width) 0.7) (* (q/height) 0.5)] 300)
    (assoc (circle :self [(* (q/width) 0.7) (* (q/height) 0.5)] 0)
           :animated? true
           :color (vec (qpu/darken common/blue)))]
   (multi-line-text
    "(fn [x]
  (x x))"
    [(* (q/width) 0.15) (- (* (q/height) 0.5) (* dy text-size))])))

(defn wrapped-sprites
  []
  (concat
   [(circle :wrapped [(* (q/width) 0.7) (* (q/height) 0.5)] 300)]
   (multi-line-text
    "(fn [x]
  (f (x x)))"
    [(* (q/width) 0.1) (- (* (q/height) 0.5) (* dy text-size))])))

(defn delayed-sprites
  []
  (concat
   [(circle :delayed [(* (q/width) 0.7) (* (q/height) 0.5)] 300)]
   (multi-line-text
    "(fn [x]
  (f (fn [y]
       ((x x) y))))"
    [(* (q/width) 0.05) (- (* (q/height) 0.5) (* dy 1.5 text-size))])))

(defn sprites
  "The initial list of sprites for this scene"
  []
  (self-sprites))

(defn draw-circles
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/grey)
  (qpsprite/draw-scene-sprites state)

  (when (:recording? state)
    (q/save-frame "output/circles-####.jpg")))

(defn update-circles
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites
      qptween/update-sprite-tweens
      qpdelay/update-delays))

(declare add-self-expansion-tween)
(declare add-self-color-tweens)

(defn add-self-expansion-tween
  [s]
  (-> s
      (assoc :color (vec (qpu/darken common/blue)))
      (qptween/add-tween
       (qptween/tween :size 300
                      :step-count 30
                      :easing-fn qptween/ease-out-sine
                      :on-complete-fn add-self-color-tweens))))

(defn add-self-color-tweens
  [{[r g b a] :color :as s}]
  (-> s
      (qptween/add-tween
       (qptween/tween
        :color (- (nth common/blue 0) r)
        :update-fn (fn [c d] (update c 0 + d))
        :step-count 20))
      (qptween/add-tween
       (qptween/tween
        :color (- (nth common/blue 1) g)
        :update-fn (fn [c d] (update c 1 + d))
        :step-count 20))
      (qptween/add-tween
       (qptween/tween
        :color (- (nth common/blue 2) b)
        :update-fn (fn [c d] (update c 2 + d))
        :step-count 20
        :on-complete-fn #(-> %
                             (assoc :size 0)
                             add-self-expansion-tween)))))

(defn add-wrapped-expansion-tween
  [s]
  (-> s
      (qptween/add-tween
       (qptween/tween
        :size 300
        :step-count 50
        :easing-fn qptween/ease-out-sine
        :on-complete-fn add-wrapped-expansion-tween))))

(defn handle-mouse-pressed
  [state e]
  (-> state
      (qpsprite/update-sprites-by-pred
       (fn [s] (and (:animated? s)
                    (= :self (:sprite-group s))))
       add-self-expansion-tween)

      (qpsprite/update-sprites-by-pred
       (qpsprite/group-pred :wrapped)
       add-wrapped-expansion-tween)

      ;; @TODO: need third case for delayed eval where we show some condition being checked?
      #_(qpsprite/update-sprites-by-pred
       (fn [s] (and (:animated? s)
                    (= :delayed (:sprite-group s))))
       add-delayed-expansion-tween)))

(defn handle-key-pressed
  [{:keys [current-scene] :as state} e]
  (cond
    (= :1 (:key e)) (qpscene/transition state :self-application)
    (= :2 (:key e)) (qpscene/transition state :wrapped-self-application)
    (= :3 (:key e)) (qpscene/transition state :circles)
    ;; (= :4 (:key e)) (qpscene/transition state :finale?)

    (= :a (:key e)) (assoc-in state [:scenes current-scene :sprites] (self-sprites))
    (= :b (:key e)) (assoc-in state [:scenes current-scene :sprites] (wrapped-sprites))
    (= :c (:key e)) (assoc-in state [:scenes current-scene :sprites] (delayed-sprites))

    (= :space (:key e)) (handle-mouse-pressed state {})

    (= :r (:key e)) (do (prn "Recording? " (not (:recording? state)))
                        (update state :recording? not))
    :else state))

(defn init
  []
  {:sprites (sprites)
   :draw-fn draw-circles
   :update-fn update-circles
   :mouse-pressed-fns [handle-mouse-pressed]
   :key-pressed-fns [handle-key-pressed]
   :next-transition 0})
