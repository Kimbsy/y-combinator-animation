(ns y-combinator-animation.scenes.circles
  (:require [quil.core :as q]
            [quip.delay :as qpdelay]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]
            [clojure.string :as s]
            [y-combinator-animation.common :as common]))

;; @TODO: we probably want this to be based on the screen size? how
;; big is the projector?????
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
  (when (< 1 size)
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
      (when (and (< 1 inner-size)
                 (< inner-size (+ (q/width) (q/height) 300)))
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
           :color (common/darken common/blue))]
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
  (let [circle-pos [(* (q/width) 0.7) (* (q/height) 0.5)]]
    (concat
     [(circle :delayed-wrapped circle-pos 300)
      (circle :delayed circle-pos 300)
      (qpsprite/text-sprite
       "c"
       (map + circle-pos [(- (* text-size 0.5 1.5))
                          (* text-size 0.5 0.5)])
       :sprite-group :conditional
       :color (nth (iterate common/darken common/blue) 2)
       :size text-size)
      (qpsprite/text-sprite
       "?"
       (map + circle-pos [(- (* text-size 0.5 0.5))
                          (* text-size 0.5 0.5)])
       :sprite-group :qmark
       :color (nth (iterate common/darken common/blue) 2)
       :size text-size)
      (qpsprite/text-sprite
       "λ"
       (map + circle-pos [(* text-size 0.5 1.5)
                          (* text-size 0.5 0.5)])
       :sprite-group :lambda
       :color (nth (iterate common/darken common/blue) 2)
       :size text-size)
      (qpsprite/text-sprite
       "42"
       (map + circle-pos [0
                          (* text-size 0.5 0.5)])
       :sprite-group :value
       :color (assoc (nth (iterate common/darken common/blue) 2) 3 0)
       :size text-size)]
     (multi-line-text
      "(fn [x]
  (f (fn [y]
       ((x x) y))))"
      [(* (q/width) 0.05) (- (* (q/height) 0.5) (* dy 1.5 text-size))]))))

(defn sprites
  "The initial list of sprites for this scene"
  []
  (self-sprites))

(defn draw-circles
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/slide-bg-blue)
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
      (assoc :color (common/darken common/blue))
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

(def color-change-step-count 10)

(defn fade-in
  [{:keys [color] :as s}]
  (common/tween-to-color
   s
   (assoc color 3 255)
   {:step-count color-change-step-count
    :easing-fn qptween/ease-out-sine}))

(defn fade-out
  [{:keys [color] :as s}]
  (common/tween-to-color
   s
   (assoc color 3 0)
   {:step-count color-change-step-count
    :easing-fn qptween/ease-out-sine}))

(defn go-white
  [s]
  (common/tween-to-color
   s
   common/white
   {:step-count color-change-step-count
    :easing-fn qptween/ease-out-sine}))

(defn go-green
  [s]
  (common/tween-to-color
   s
   common/green
   {:step-count color-change-step-count
    :easing-fn qptween/ease-out-sine}))

(defn go-dark-blue
  [s]
  (common/tween-to-color
   s
   (nth (iterate common/darken common/blue) 2)
   {:step-count color-change-step-count
    :easing-fn qptween/ease-out-sine}))

(defn go-red
  [s]
  (common/tween-to-color
   s
   common/red
   {:step-count color-change-step-count
    :easing-fn qptween/ease-out-sine}))

(defn activate-text
  [s]
  (-> s
      (qptween/add-tween
       (qptween/tween
        :pos
        20
        :update-fn qptween/tween-y-fn
        :yoyo? true
        :yoyo-update-fn qptween/tween-y-yoyo-fn
        :easing-fn qptween/ease-out-sine
        :step-count 5))
      go-white))

(defn add-delayed-expansion-tween
  [s]
  (-> s
      (qptween/add-tween
       (qptween/tween
        :size 300
        :step-count 10
        :easing-fn qptween/ease-out-sine))))

(defn add-delayed-collapse-tween
  [s]
  (-> s
      (qptween/add-tween
       (qptween/tween
        :size -300
        :step-count 10
        :easing-fn qptween/ease-out-sine))))

(defn deactivate-all
  [state]
  (-> state
      (qpsprite/update-sprites-by-pred
       (common/groups-pred [:qmark :conditional :lambda])
       go-dark-blue)))

(def expansion-cycles 3)

(declare init-collapse-animation)

(def collapse-animation-sequence
  [;; check condition
   (fn [state]
     (-> state
         (qpsprite/update-sprites-by-pred
          (common/groups-pred [:qmark :conditional])
          activate-text)))

   ;; go red
   (fn [state]
     (qpsprite/update-sprites-by-pred
      state
      (common/groups-pred [:qmark :conditional])
      go-red))

   ;; turn into value
   ;; hide c? λ
   (fn [state]
     (qpsprite/update-sprites-by-pred
      state
      (common/groups-pred [:qmark :conditional :lambda])
      fade-out))
   ;; make front circle green
   (fn [state]
     (qpsprite/update-sprites-by-pred
      state
      (common/groups-pred [:delayed])
      go-green))
   ;; show value
   (fn [state]
     (qpsprite/update-sprites-by-pred
      state
      (common/groups-pred [:value])
      fade-in))

   ;; shrink wrapper circle
   (fn [state]
     (qpsprite/update-sprites-by-pred
      state
      (qpsprite/group-pred :delayed-wrapped)
      add-delayed-collapse-tween))

   ;; loop if not done
   (fn [{:keys [delayed-animation-cycles] :as state}]
     (if (= :c (:variant state))
       (if (< 0 delayed-animation-cycles)
         (-> state
             (update :delayed-animation-cycles dec)
             (init-collapse-animation :continuing? true))
         (-> state
             ;; remove all circles
             ((fn [st]
                (qpsprite/update-sprites-by-pred
                 st
                 (common/groups-pred [:delayed :delayed-wrapped])
                 add-delayed-collapse-tween)))
             ;; make value green
             ((fn [st]
                (qpsprite/update-sprites-by-pred
                 st
                 (common/groups-pred [:value])
                 go-green)))))
       state))])

(defn init-collapse-animation
  [{:keys [current-scene] :as state} & {:keys [continuing?] :or {continuing? false}}]
  (-> state
      (assoc-in [:scenes current-scene :delays]
                (qpdelay/sequential-delays
                 (let [ds [0 30 50 0 0 50 30]]
                   (if continuing?
                     (map (fn [d f] [d f])
                          [30 30]
                          (drop 5 collapse-animation-sequence))
                     (map (fn [d f] [d f])
                          ds
                          collapse-animation-sequence)))))))

(declare init-delayed-animation)

(def delayed-animation-sequence
  [;; check condition
   (fn [state]
     (-> state
         (qpsprite/update-sprites-by-pred
          (common/groups-pred [:qmark :conditional])
          activate-text)))

   ;; go green
   (fn [state]
     (qpsprite/update-sprites-by-pred
      state
      (common/groups-pred [:qmark :conditional])
      go-green))

   ;; activate lambda
   (fn [state]
     (qpsprite/update-sprites-by-pred
      state
      (qpsprite/group-pred :lambda)
      activate-text))

   ;; expand circle
   (fn [state]
     (qpsprite/update-sprites-by-pred
      state
      (qpsprite/group-pred :delayed-wrapped)
      add-delayed-expansion-tween))

   ;; deactivate-all
   (fn [state]
     (deactivate-all state))

   ;; loop if we're still on variant `c`
   (fn [{:keys [delayed-animation-cycles] :as state}]
     (if (= :c (:variant state))
       (if (< delayed-animation-cycles (dec expansion-cycles))
         (-> state
             (update :delayed-animation-cycles inc)
             init-delayed-animation)
         (init-collapse-animation state))
       state))])

(defn init-delayed-animation
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :delays]
                (qpdelay/sequential-delays
                 (map (fn [d f] [d f])
                      [0 30 50 0 20 50]
                      delayed-animation-sequence)))))

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
           init-delayed-animation)

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
                        (assoc-in [:scenes current-scene :delays] [])
                        (assoc-in [:scenes current-scene :sprites] (self-sprites)))
    (= :b (:key e)) (-> state
                        (assoc :variant :b)
                        (assoc-in [:scenes current-scene :delays] [])
                        (assoc-in [:scenes current-scene :sprites] (wrapped-sprites)))
    (= :c (:key e)) (-> state
                        (assoc :variant :c)
                        (assoc :delayed-animation-cycles 0)
                        (assoc-in [:scenes current-scene :delays] [])
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
