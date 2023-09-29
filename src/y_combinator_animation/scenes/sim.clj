(ns y-combinator-animation.scenes.sim
  (:require [quil.core :as q]
            [quip.delay :as qpdelay]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]
            [clojure.string :as s]
            [y-combinator-animation.common :as common]))

;; @TODO: need ot figure out a nice way of describing a transition


;; maybe we just want the characters to be individual text sprites and
;; not be contained in a compound sprite? each cant have :pos [x y] on
;; it and we can update specific ones by that. once the transform is
;; complete we can replace them all with a new set of sprites so we
;; have a clean slate.

(declare init)

(def text-size (* 2 qpu/default-text-size))
(def green [178 255 99 255])
(def blue [144 180 253 255])
(def white [230 230 230 255])

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

(def self-appl "((f [x] (x x)) (f [x] (x x)))")

(defn sprites
  "The initial list of sprites for this scene"
  [content]
  (concat
   (init-chars [(/ (q/width) 2)
                (/ (q/height) 2)]
               content
               white
               text-size
               qpu/default-font)
   (map
    (fn [s]
      (assoc s :sprite-group :duplicate))
    (init-chars [(/ (q/width) 2)
                 (/ (q/height) 2)]
                content
                white
                text-size
                qpu/default-font))))

(defn draw-sim
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/grey)

  ;; draw cross at mouse pos for alignment debugging
  (q/stroke 255)
  (q/line 0 (q/mouse-y) (q/width) (q/mouse-y))
  (q/line (q/mouse-x) 0 (q/mouse-x) (q/height))
  
  (qpsprite/draw-scene-sprites state))

(defn update-sim
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
                         (if (and (not= :duplicate (:sprite-group s))
                                  ((set (range 15 28)) cx))
                           (-> s
                               (assoc :color green)
                               add-hide-tween)
                           s))
                       sprites))))

   ;; shrink box
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos [(/ (q/width) 2)
                                        (/ (q/height) 2)]
                                       content
                                       text-size)
           p15 (char-pos center-pos [15 0] text-size)
           p16 (char-pos center-pos [16 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (if (not= :duplicate (:sprite-group s))
                             (cond
                               (<= 15 cx 27) (qptween/add-tween
                                              s
                                              (qptween/tween
                                               :pos
                                               (- (first p15) spx)
                                               :update-fn qptween/tween-x-fn
                                               :step-count 30))
                               (= 28 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p16) spx)
                                           :update-fn qptween/tween-x-fn
                                           :step-count 30))
                               :else s)
                             s))
                         sprites)))))

   ;; move green to arg position
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos [(/ (q/width) 2)
                                        (/ (q/height) 2)]
                                       content
                                       text-size)
           p15 (char-pos center-pos [15 0] text-size)
           p5 (char-pos center-pos [5 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (if (not= :duplicate (:sprite-group s))
                             (cond
                               (<= 15 cx 27) (-> s
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
                                                   (- (first p5) spx)
                                                   :easing-fn qptween/ease-in-out-sine
                                                   :update-fn qptween/tween-x-fn
                                                   :step-count 50)))
                               (#{0 28} cx) (qptween/add-tween
                                             s
                                             (qptween/tween
                                              :color
                                              -255
                                              :update-fn (fn [c d] (update c 3 + d))
                                              :step-count 30))
                               :else s)
                             s))
                         sprites)))))

   ;; make xs green (9 and 11)
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords
                             [spx spy :as spos] :pos
                             [r g b a] :color
                             :as s}]
                         (if (not= :duplicate (:sprite-group s))
                           (cond
                             (#{9 11} cx) (-> s
                                              (qptween/add-tween
                                               (qptween/tween
                                                :color
                                                (- (nth green 0) r)
                                                :update-fn (fn [c d] (update c 0 + d))
                                                :step-count 30))
                                              (qptween/add-tween
                                               (qptween/tween
                                                :color
                                                (- (nth green 1) g)
                                                :update-fn (fn [c d] (update c 1 + d))
                                                :step-count 30))
                                              (qptween/add-tween
                                               (qptween/tween
                                                :color
                                                (- (nth green 2) b)
                                                :update-fn (fn [c d] (update c 2 + d))
                                                :step-count 30)))
                             (or (< cx 8)
                                 (< 12 cx)) (qptween/add-tween
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
           center-pos (calc-center-pos [(/ (q/width) 2)
                                        (/ (q/height) 2)]
                                       content
                                       text-size)
           p0 (char-pos center-pos [0 0] text-size)
           p7 (char-pos center-pos [7 0] text-size)
           p21 (char-pos center-pos [21 0] text-size)
           p28 (char-pos center-pos [28 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (if (not= :duplicate (:sprite-group s))
                             (cond
                               (= 8 cx) (qptween/add-tween
                                         s
                                         (qptween/tween
                                          :pos
                                          (- (first p0) spx)
                                          :update-fn qptween/tween-x-fn
                                          :step-count 30))
                               (= 9 cx) (qptween/add-tween
                                         s
                                         (qptween/tween
                                          :pos
                                          (- (first p7) spx)
                                          :update-fn qptween/tween-x-fn
                                          :step-count 30))
                               (= 11 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p21) spx)
                                           :update-fn qptween/tween-x-fn
                                           :step-count 30))
                               (= 12 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p28) spx)
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
                             (if (and (not= :duplicate (:sprite-group s))
                                      (#{9 11} cx))
                               (-> s
                                   (assoc :color green)
                                   add-hide-tween)
                               s))
                           sprites)))
         ;; add more letters
         (qpdelay/add-delay
          (qpdelay/delay
            30
            (fn [{:keys [current-scene] :as state}]
              (let [content (get-in state [:scenes current-scene :content])
                    center-pos (calc-center-pos [(/ (q/width) 2)
                                                 (/ (q/height) 2)]
                                                content
                                                text-size)
                    p7 (char-pos center-pos [7 0] text-size)
                    p21 (char-pos center-pos [21 0] text-size)]
                (update-in state [:scenes current-scene :sprites]
                           (fn [sprites]
                             (concat
                              (remove (fn [{[cx cy] :init-coords :as s}]
                                        (and (not= :duplicate (:sprite-group s))
                                             (#{9 11} cx)))
                                      sprites)
                              (map (fn [cx c]
                                     (-> (hidable-text-sprite
                                          (str c)
                                          p7
                                          green
                                          qpu/default-font
                                          text-size)
                                         (assoc :init-coords [cx 0])
                                         (assoc :hidden 1)))
                                   (range 1 14)
                                   "(f [x] (x x))")
                              (map (fn [cx c]
                                     (-> (hidable-text-sprite
                                          (str c)
                                          p21
                                          green
                                          qpu/default-font
                                          text-size)
                                         (assoc :init-coords [cx 0])
                                         (assoc :hidden 1)))
                                   (range 15 28)
                                   "(f [x] (x x))"))))))))))
   

   ;; spread them out
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos [(/ (q/width) 2)
                                        (/ (q/height) 2)]
                                       content
                                       text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (if (and (not= :duplicate (:sprite-group s))
                                    (= green (:color s))
                                    (or (<= 1 cx 13)
                                        (<= 15 cx 27)))
                             (qptween/add-tween
                              s
                              (qptween/tween
                               :pos
                               (- (first (char-pos center-pos coords text-size)) spx)
                               :update-fn qptween/tween-x-fn
                               :step-count 30))
                             s))
                         sprites)))))

   ;; remove box
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords :as s}]
                         (if (not= :duplicate (:sprite-group s))
                           (-> s
                               add-show-tween)
                           s))
                       sprites))))

   ;; fade to white
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords
                             [spx spy :as spos] :pos
                             [r g b a] :color
                             :as s}]
                         (if (not= :duplicate (:sprite-group s))
                           (-> s
                               (qptween/add-tween
                                (qptween/tween
                                 :color
                                 (- (nth white 0) r)
                                 :update-fn (fn [c d] (update c 0 + d))
                                 :step-count 30))
                               (qptween/add-tween
                                (qptween/tween
                                 :color
                                 (- (nth white 1) g)
                                 :update-fn (fn [c d] (update c 1 + d))
                                 :step-count 30))
                               (qptween/add-tween
                                (qptween/tween
                                 :color
                                 (- (nth white 2) b)
                                 :update-fn (fn [c d] (update c 2 + d))
                                 :step-count 30)))
                           s))
                       sprites))))

   ;; merge duplicate
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
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
                       sprites))))])

(defn handle-mouse-pressed
  [{:keys [current-scene] :as state} e]
  (let [tn (get-in state [:scenes current-scene :next-transition])]
    (if (< tn (count transitions))
      (let [t-fn (nth transitions tn)]
        (-> (t-fn state)
            (update-in [:scenes current-scene :next-transition] inc)))
      (assoc-in state [:scenes current-scene] (init)))))

(defn init
  "Initialise this scene"
  []
  (let [initial-content self-appl]
    {:sprites (sprites initial-content)
     :draw-fn draw-sim
     :update-fn update-sim
     :mouse-pressed-fns [handle-mouse-pressed]
     :next-transition 0
     :content initial-content}))
