(ns y-combinator-animation.scenes.self-application
  (:require [quil.core :as q]
            [quip.delay :as qpdelay]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]
            [clojure.string :as s]
            [y-combinator-animation.common :as common]))

(declare init)

(def text-size (* 2 qpu/default-text-size))

;; magic variable for pleasant vertical line spacing
(def dy 1.3)

(defn calc-center-pos
  [pos content size]
  (let [lines (s/split-lines content)
        y-offset (* (count lines) size dy 0.5)
        max-length (apply max (map count lines))
        x-offset (* max-length size 0.5 0.5)]
    (map - pos [x-offset y-offset])))

(defn char-pos
  [[px py :as parent-pos] [cx cy :as char-coords] size]
  [(+ px (* cx size 0.5))
   (+ py (* cy size dy))])

(defn add-hide-tween
  [s]
  (qptween/add-tween
   s
   (qptween/tween :hidden 1
                  :update-fn (fn [h d]
                               (max (min (+ h d) 1) 0))
                  :step-count 30
                  :easing-fn qptween/ease-in-out-expo)))

(defn add-show-tween
  [s]
  (qptween/add-tween
   s
   (qptween/tween :hidden -1
                  :update-fn (fn [h d]
                               (max (min (+ h d) 1) 0))
                  :step-count 30
                  :easing-fn qptween/ease-in-out-expo)))

(defn hidable-text-sprite
  [content pos color font size]
  (-> (qpsprite/text-sprite content pos :color color :font font :size size :offsets [:left :top])
      (assoc :hidden 0)
      (assoc :size size)
      (assoc :draw-fn (fn [{:keys [size color hidden] [x y] :pos :as s}]
                        (qpsprite/draw-text-sprite s)
                        (q/no-stroke)
                        (qpu/fill color)
                        (q/rect (dec x) y (+ 2(/ size 2)) (* hidden (inc size)))))))

(defn init-chars
  [parent-pos content color size font]
  (let [[px py :as parent-pos] (calc-center-pos parent-pos content size)
        lines (s/split-lines content)]
    (apply concat
           (map-indexed
            (fn [i l]
              (keep-indexed
               (fn [j c]
                 (-> (hidable-text-sprite
                      (str c)
                      (char-pos parent-pos [j i] size)
                      color
                      font
                      text-size)
                     (assoc :init-coords [j i])))
               l))
            lines))))

(def y-str "(def Y (fn [f]
         ((fn [x]
            (x x))
          (fn [x]
            (f (x x))))))")

(def self-appl "((fn [x] (x x)) (fn [x] (x x)))")

(defn content-pos
  []
  [(* (q/width) 0.5)
   (* (q/height) 0.5)])

(defn ref-pos
  []
  [(* (q/width) 0.25)
   (* (q/height) 0.75)])

(defn sprites
  "The initial list of sprites for this scene"
  [content]
  (concat
   (init-chars (content-pos)
               content
               common/white
               text-size
               qpu/default-font)

   ;; duplicate
   (map
    (fn [s]
      (assoc s :sprite-group :duplicate))
    (init-chars (content-pos)
                content
                common/white
                text-size
                qpu/default-font))
   ;; reference
   (let [rs (map (fn [s]
                     (assoc s :sprite-group :reference))
                   (init-chars (ref-pos)
                               "              "
                               common/green
                               text-size
                               qpu/default-font))
         p7 (:pos (nth rs 7))]
     (map (fn [s]
            (assoc s :pos p7))
          rs))))

(defn draw-self-application
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/grey)
  (qpsprite/draw-scene-sprites state)

  (when (:recording? state)
    (q/save-frame "output/self-application-####.jpg")))

(defn update-self-application
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites
      qptween/update-sprite-tweens
      qpdelay/update-delays))

(defn random-tween-x
  []
  (qptween/tween
   :pos
   (- 20 (rand-int 40))
   :step-count 20
   :update-fn qptween/tween-x-fn
   :yoyo-update-fn qptween/tween-x-yoyo-fn
   :yoyo? true
   :repeat-times (inc (rand-int 3))))

(defn random-tween-y
  []
  (qptween/tween
   :pos
   (- 20 (rand-int 40))
   :step-count 20
   :update-fn qptween/tween-y-fn
   :yoyo-update-fn qptween/tween-y-yoyo-fn
   :yoyo? true
   :repeat-times (inc (rand-int 3))))

(def transitions
  [;; duplicate
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords :as s}]
                         (if (= :duplicate (:sprite-group s))
                           (-> s
                               (qptween/add-tween
                                (qptween/tween
                                 :pos
                                 -100
                                 :update-fn qptween/tween-y-fn
                                 :step-count 20))
                               (qptween/add-tween
                                (qptween/tween
                                 :color
                                 -150
                                 :update-fn (fn [c d] (update c 3 + d))
                                 :step-count 20)))
                           s))
                       sprites))))

   ;; hide with box
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords :as s}]
                         (if (or (and (not (#{:duplicate :reference} (:sprite-group s)))
                                      ((set (range 16 30)) cx))
                                 (= :reference (:sprite-group s)))
                           (-> s
                               (assoc :color common/green)
                               add-hide-tween)
                           s))
                       sprites))))

   ;; shrink/expand box
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p16 (char-pos center-pos [16 0] text-size)
           p17 (char-pos center-pos [17 0] text-size)

           ref-center-pos (calc-center-pos (ref-pos)
                                           "(fn [x] (x x))"
                                           text-size)]
       (-> state
           (update-in [:scenes current-scene :sprites]
                      (fn [sprites]
                        (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                               (cond
                                 (and (not (#{:duplicate :reference} (:sprite-group s)))
                                      (<= 16 cx 29))
                                 (qptween/add-tween
                                  s
                                  (qptween/tween
                                   :pos
                                   (- (first p16) spx)
                                   :update-fn qptween/tween-x-fn
                                   :step-count 30))

                                 (and (not (#{:duplicate :reference} (:sprite-group s)))
                                      (= 30 cx))
                                 (qptween/add-tween
                                  s
                                  (qptween/tween
                                   :pos
                                   (- (first p17) spx)
                                   :update-fn qptween/tween-x-fn
                                   :step-count 30))

                                 (= :reference (:sprite-group s))
                                 (qptween/add-tween
                                  s
                                  (qptween/tween
                                   :pos
                                   (- (first (char-pos ref-center-pos coords text-size)) spx)
                                   :update-fn qptween/tween-x-fn
                                   :step-count 30))
                                 :else s))
                             sprites)))
           (qpdelay/add-delay
            (qpdelay/delay
              30
              (fn [{:keys [current-scene] :as state}]
                (update-in state [:scenes current-scene :sprites]
                           (fn [sprites]
                             (concat
                              (->> sprites
                                   (filter (qpsprite/group-pred :reference))
                                   (sort-by (comp first :coords))
                                   (map (fn [c s]
                                          (assoc s :content (str c)))
                                        "(fn [x] (x x))"))
                              (remove (qpsprite/group-pred :reference) sprites))))))))))

   ;; move green to arg position
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p16 (char-pos center-pos [16 0] text-size)
           p6 (char-pos center-pos [6 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (cond
                             (and (not (#{:duplicate :reference} (:sprite-group s)))
                                  (<= 16 cx 29))
                             (-> s
                                 (qptween/add-tween
                                  (qptween/tween
                                   :pos
                                   100
                                   :easing-fn qptween/ease-in-out-sine
                                   :yoyo? true
                                   :update-fn qptween/tween-y-fn
                                   :yoyo-update-fn qptween/tween-y-yoyo-fn
                                   :step-count 25))
                                 (qptween/add-tween
                                  (qptween/tween
                                   :pos
                                   (- (first p6) spx)
                                   :easing-fn qptween/ease-in-out-sine
                                   :update-fn qptween/tween-x-fn
                                   :step-count 50)))
                             
                             (and (not (#{:duplicate :reference} (:sprite-group s)))
                                  (#{0 30} cx))
                             (qptween/add-tween
                              s
                              (qptween/tween
                               :color
                               -255
                               :update-fn (fn [c d] (update c 3 + d))
                               :step-count 30))

                             (= :reference (:sprite-group s)) (add-show-tween s)
                             
                             :else s))
                         sprites)))))

   ;; make xs green (10 and 12)
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords
                             [spx spy :as spos] :pos
                             [r g b a] :color
                             :as s}]
                         (if (not (#{:duplicate :reference} (:sprite-group s)))
                           (cond
                             (#{10 12} cx) (-> s
                                               (qptween/add-tween
                                                (qptween/tween
                                                 :color
                                                 (- (nth common/green 0) r)
                                                 :update-fn (fn [c d] (update c 0 + d))
                                                 :step-count 30))
                                               (qptween/add-tween
                                                (qptween/tween
                                                 :color
                                                 (- (nth common/green 1) g)
                                                 :update-fn (fn [c d] (update c 1 + d))
                                                 :step-count 30))
                                               (qptween/add-tween
                                                (qptween/tween
                                                 :color
                                                 (- (nth common/green 2) b)
                                                 :update-fn (fn [c d] (update c 2 + d))
                                                 :step-count 30)))
                             (or (< cx 9)
                                 (< 13 cx)) (qptween/add-tween
                                             s
                                             (qptween/tween
                                              :color
                                              -255
                                              :update-fn (fn [c d] (update c 3 + d))
                                              :step-count 30))
                             :else s)
                           s))
                       sprites))))

   ;; move xs and parens
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p0 (char-pos center-pos [0 0] text-size)
           p8 (char-pos center-pos [8 0] text-size)
           p23 (char-pos center-pos [23 0] text-size)
           p30 (char-pos center-pos [30 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (if (not (#{:duplicate :reference} (:sprite-group s)))
                             (cond
                               (= 9 cx) (qptween/add-tween
                                         s
                                         (qptween/tween
                                          :pos
                                          (- (first p0) spx)
                                          :update-fn qptween/tween-x-fn
                                          :step-count 30))
                               (= 10 cx) (qptween/add-tween
                                         s
                                         (qptween/tween
                                          :pos
                                          (- (first p8) spx)
                                          :update-fn qptween/tween-x-fn
                                          :step-count 30))
                               (= 12 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p23) spx)
                                           :update-fn qptween/tween-x-fn
                                           :step-count 30))
                               (= 13 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p30) spx)
                                           :update-fn qptween/tween-x-fn
                                           :step-count 30))
                               :else s)
                             s))
                         sprites)))))

   ;; hide with box
   (fn [{:keys [current-scene] :as state}]
     (-> state
         (update-in [:scenes current-scene :sprites]
                    (fn [sprites]
                      (map (fn [{[cx cy :as coords] :init-coords :as s}]
                             (if (and (not (#{:duplicate :reference} (:sprite-group s)))
                                      (#{10 12} cx))
                               (-> s
                                   (assoc :color common/green)
                                   add-hide-tween)
                               s))
                           sprites)))
         ;; add more letters
         (qpdelay/add-delay
          (qpdelay/delay
            30
            (fn [{:keys [current-scene] :as state}]
              (let [content (get-in state [:scenes current-scene :content])
                    center-pos (calc-center-pos (content-pos)
                                                content
                                                text-size)
                    p8 (char-pos center-pos [8 0] text-size)
                    p23 (char-pos center-pos [23 0] text-size)]
                (update-in state [:scenes current-scene :sprites]
                           (fn [sprites]
                             (concat
                              (remove (fn [{[cx cy] :init-coords :as s}]
                                        (and (not (#{:duplicate :reference} (:sprite-group s)))
                                             (#{10 12} cx)))
                                      sprites)
                              (map (fn [cx c]
                                     (-> (hidable-text-sprite
                                          (str c)
                                          p8
                                          common/green
                                          qpu/default-font
                                          text-size)
                                         (assoc :init-coords [cx 0])
                                         (assoc :hidden 1)))
                                   (range 1 15)
                                   "(fn [x] (x x))")
                              (map (fn [cx c]
                                     (-> (hidable-text-sprite
                                          (str c)
                                          p23
                                          common/green
                                          qpu/default-font
                                          text-size)
                                         (assoc :init-coords [cx 0])
                                         (assoc :hidden 1)))
                                   (range 16 30)
                                   "(fn [x] (x x))"))))))))))
   

   ;; spread them out
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (cond
                             (and (not (#{:duplicate :reference} (:sprite-group s)))
                                  (= common/green (:color s))
                                  (or (<= 1 cx 14)
                                      (<= 16 cx 29)))
                             (qptween/add-tween
                              s
                              (qptween/tween
                               :pos
                               (- (first (char-pos center-pos coords text-size)) spx)
                               :update-fn qptween/tween-x-fn
                               :step-count 30))

                             (= :reference (:sprite-group s))
                             (add-hide-tween s)
                             
                             :else s))
                         sprites)))))

   ;; remove box
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords :as s}]
                         (cond
                           (not (#{:duplicate :reference} (:sprite-group s)))
                           (add-show-tween s)

                           (= :reference (:sprite-group s))
                           (-> s
                               (assoc :content " ")
                               add-show-tween)
                           
                           :else s))
                       sprites))))

   ;; fade to white, remove ref
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords
                             [spx spy :as spos] :pos
                             [r g b a] :color
                             :as s}]
                         (cond
                           (not (#{:duplicate :reference} (:sprite-group s)))
                           (-> s
                               (qptween/add-tween
                                (qptween/tween
                                 :color
                                 (- (nth common/white 0) r)
                                 :update-fn (fn [c d] (update c 0 + d))
                                 :step-count 30))
                               (qptween/add-tween
                                (qptween/tween
                                 :color
                                 (- (nth common/white 1) g)
                                 :update-fn (fn [c d] (update c 1 + d))
                                 :step-count 30))
                               (qptween/add-tween
                                (qptween/tween
                                 :color
                                 (- (nth common/white 2) b)
                                 :update-fn (fn [c d] (update c 2 + d))
                                 :step-count 30)))                           
                           
                           :else s))
                       sprites))))

   ;; merge duplicate
   (fn [{:keys [current-scene] :as state}]
     (-> state
         (update-in [:scenes current-scene :sprites]
                    (fn [sprites]
                      (map (fn [{[cx cy :as coords] :init-coords :as s}]
                             (if (= :duplicate (:sprite-group s))
                               (-> s
                                   (qptween/add-tween
                                    (qptween/tween
                                     :pos
                                     100
                                     :update-fn qptween/tween-y-fn
                                     :step-count 20))
                                   (qptween/add-tween
                                    (qptween/tween
                                     :color
                                     150
                                     :update-fn (fn [c d] (update c 3 + d))
                                     :step-count 20)))
                               s))
                           sprites)))))])

(defn handle-mouse-pressed
  [{:keys [current-scene] :as state} e]
  (if (= :left (:button e))
    ;; manual transition
    (let [tn (get-in state [:scenes current-scene :next-transition])]
      (if (< tn (count transitions))
        (let [t-fn (nth transitions tn)]
          (-> (t-fn state)
              (update-in [:scenes current-scene :next-transition] inc)))
        (assoc-in state [:scenes current-scene] (init))))
    ;; automatic transitions
    (-> state
        (assoc-in [:scenes current-scene] (init))
        (assoc-in [:scenes current-scene :delays]
                  (qpdelay/sequential-delays
                   (map (fn [d f] [d f])
                        [74 80 38 65 105 86 75 31 40 49 49]
                        transitions))))))

(defn handle-key-pressed
  [state e]
  (cond
    (= :1 (:key e)) (qpscene/transition state :self-application)
    (= :2 (:key e)) (qpscene/transition state :wrapped-self-application)
    (= :3 (:key e)) (qpscene/transition state :circles)
    ;; (= :4 (:key e)) (qpscene/transition state :finale?)
    (= :r (:key e)) (do (prn "Recording? " (not (:recording? state)))
                        (update state :recording? not))
    :else state))

(defn init
  "Initialise this scene"
  []
  (let [initial-content self-appl]
    {:sprites (sprites initial-content)
     :draw-fn draw-self-application
     :update-fn update-self-application
     :mouse-pressed-fns [handle-mouse-pressed]
     :key-pressed-fns [handle-key-pressed]
     :next-transition 0
     :content initial-content}))
