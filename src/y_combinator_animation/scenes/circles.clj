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

;; This scene compares the evaluation of our different expressions
;; using helpful graphical representations.

;; The self application lambda evaluates to itself

;; The wrapped self evaluation lambda continuously wraps itself

;; The delayed self evaluation lambda has a reference to a recur-fn
;; and decides to invoke it based on a condition, when invoked it
;; wraps itself.

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
  (draw-self-circle s))

(defn draw-delayed-wrapped-circle
  [s]
  (draw-wrapped-circle s))

(defn circle
  [sprite-group pos size]
  {:sprite-group sprite-group
   :draw-fn (sprite-group {:self draw-self-circle
                           :wrapped draw-wrapped-circle
                           :delayed draw-delayed-circle
                           :delayed-wrapped draw-delayed-wrapped-circle})
   :update-fn identity
   :pos pos
   :color common/blue
   :stroke-color common/white
   :size size})

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
   [(circle :delayed-wrapped [(* (q/width) 0.7) (* (q/height) 0.5)] 300)
    (circle :delayed [(* (q/width) 0.7) (* (q/height) 0.5)] 300)]
   (multi-line-text
    "(fn [x]
  (f (fn [y]
       ((x x) y))))
λ"
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

;; The delayed self evaluation lambda has a reference to a recur-fn
;; and decides to invoke it based on a condition, when invoked it
;; wraps itself.



;; maybe we want to show a question mark representing the condition?

;; maybe we want a lambda symbol to represent the recur-fn?

;; pulse the question mark green, then pulse the lambda, then wrap?

;; maybe the condition should be something else, then we can show
;; we're testing the condition by adding a question mark, then show
;; we've evaluated it by either going green or red?

;; so like a `c` and a `λ`

; ;can we draw a lambda?


(defn add-delayed-tweens
  [s]
  s)

(defn handle-mouse-pressed
  [{:keys [variant current-scene] :as state} e]
  (case variant
    :a (-> state
           (assoc-in [:scenes current-scene :sprites] (self-sprites))
           (qpsprite/update-sprites-by-pred
            (fn [s] (and (:animated? s)
                         (= :self (:sprite-group s))))
            add-self-expansion-tween))

    :b (-> state
           (assoc-in [:scenes current-scene :sprites] (wrapped-sprites))
           (qpsprite/update-sprites-by-pred
            (qpsprite/group-pred :wrapped)
            add-wrapped-expansion-tween))

    :c (-> state
           (assoc-in [:scenes current-scene :sprites] (delayed-sprites))
           (qpsprite/update-sprites-by-pred
            (qpsprite/group-pred :delayed-wrapped)
            add-delayed-tweens))

    nil state))

(defn handle-key-pressed
  [{:keys [current-scene] :as state} e]
  (cond
    (= :1 (:key e)) (qpscene/transition state :self-application)
    (= :2 (:key e)) (qpscene/transition state :wrapped-self-application)
    (= :3 (:key e)) (qpscene/transition state :circles)
    ;; (= :4 (:key e)) (qpscene/transition state :finale?)

    (= :a (:key e)) (-> state
                        (assoc :variant :a)
                        (assoc-in [:scenes current-scene :sprites] (self-sprites)))
    (= :b (:key e)) (-> state
                        (assoc :variant :b)
                        (assoc-in [:scenes current-scene :sprites] (wrapped-sprites)))
    (= :c (:key e)) (-> state
                        (assoc :variant :c)
                        (assoc-in [:scenes current-scene :sprites] (delayed-sprites)))

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
