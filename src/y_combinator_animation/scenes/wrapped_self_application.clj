(ns y-combinator-animation.scenes.wrapped-self-application
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
(def blue [84 120 193 195])
(def orange [245 143 41 255])
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

(def self-appl "((fn [x] (x x)) (fn [x] (x x)))")
(def wrapped-self-appl "((fn [x] (f (x x))) (fn [x] (f (x x))))")

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
               white
               text-size
               qpu/default-font)

   ;; duplicate
   (map
    (fn [s]
      (assoc s :sprite-group :duplicate))
    (init-chars (content-pos)
                content
                white
                text-size
                qpu/default-font))
   ;; reference
   (let [rs (map (fn [s]
                   (assoc s :sprite-group :reference))
                 (init-chars (ref-pos)
                             "                  "
                             green
                             text-size
                             qpu/default-font))
         p7 (:pos (nth rs 7))]
     (map (fn [s]
            (assoc s :pos p7))
          rs))))

(defn draw-wrapped-self-application
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/grey)  
  (qpsprite/draw-scene-sprites state)

  (when (:recording? state)
    (q/save-frame "output/wrapped-self-application-####.jpg")))

(defn update-wrapped-self-application
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
                                      ((set (range 20 38)) cx))
                                 (= :reference (:sprite-group s)))
                           (-> s
                               (assoc :color green)
                               add-hide-tween)
                           s))
                       sprites))))

   ;; shrink/expand box
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p20 (char-pos center-pos [20 0] text-size)
           p21 (char-pos center-pos [21 0] text-size)

           ref-center-pos (calc-center-pos (ref-pos)
                                           "(fn [x] (f (x x)))"
                                           text-size)]
       (-> state
           (update-in [:scenes current-scene :sprites]
                      (fn [sprites]
                        (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                               (cond
                                 (and (not (#{:duplicate :reference} (:sprite-group s)))
                                      (<= 20 cx 37))
                                 (qptween/add-tween
                                  s
                                  (qptween/tween
                                   :pos
                                   (- (first p20) spx)
                                   :update-fn qptween/tween-x-fn
                                   :step-count 30))

                                 (and (not (#{:duplicate :reference} (:sprite-group s)))
                                      (= 38 cx))
                                 (qptween/add-tween
                                  s
                                  (qptween/tween
                                   :pos
                                   (- (first p21) spx)
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
                                        "(fn [x] (f (x x)))"))
                              (remove (qpsprite/group-pred :reference) sprites))))))))))

   ;; move green to arg position
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p20 (char-pos center-pos [20 0] text-size)
           p6 (char-pos center-pos [6 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (cond
                             (and (not (#{:duplicate :reference} (:sprite-group s)))
                                  (<= 20 cx 37))
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
                                  (#{0 38} cx))
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

   ;; make xs green (13 and 15)
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                (fn [sprites]
                  (map (fn [{[cx cy :as coords] :init-coords
                             [spx spy :as spos] :pos
                             [r g b a] :color
                             :as s}]
                         (if (not (#{:duplicate :reference} (:sprite-group s)))
                           (cond
                             (#{13 15} cx) (-> s
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
                             (or (< cx 9)
                                 (< 17 cx)) (qptween/add-tween
                                             s
                                             (qptween/tween
                                              :color
                                              -255
                                              :update-fn (fn [c d] (update c 3 + d))
                                              :step-count 30))
                             :else s)
                           s))
                       sprites))))

   ;; spread out 
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p-3 (char-pos center-pos [-3 0] text-size)
           p-2 (char-pos center-pos [-2 0] text-size)
           p0 (char-pos center-pos [0 0] text-size)
           p10 (char-pos center-pos [10 0] text-size)
           p29 (char-pos center-pos [29 0] text-size)
           p38 (char-pos center-pos [38 0] text-size)
           p39 (char-pos center-pos [39 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (map (fn [{[cx cy :as coords] :init-coords [spx spy :as spos] :pos :as s}]
                           (if (not (#{:duplicate :reference} (:sprite-group s)))
                             (cond
                               ;; (
                               (= 9 cx) (-> s
                                            (assoc :sprite-group :outer)
                                            (qptween/add-tween
                                             (qptween/tween
                                              :pos
                                              (- (first p-3) spx)
                                              :update-fn qptween/tween-x-fn
                                              :step-count 30)))

                               ;; f
                               (= 10 cx) (-> s
                                             (assoc :sprite-group :outer)
                                             (qptween/add-tween
                                              (qptween/tween
                                               :pos
                                               (- (first p-2) spx)
                                               :update-fn qptween/tween-x-fn
                                               :step-count 30)))

                               ;; (
                               (= 12 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p0) spx)
                                           :update-fn qptween/tween-x-fn
                                           :step-count 30))

                               ;; x
                               (= 13 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p10) spx)
                                           :update-fn qptween/tween-x-fn
                                           :step-count 30))

                               ;; x
                               (= 15 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p29) spx)
                                           :update-fn qptween/tween-x-fn
                                           :step-count 30))

                               ;; )
                               (= 16 cx) (qptween/add-tween
                                          s
                                          (qptween/tween
                                           :pos
                                           (- (first p38) spx)
                                           :update-fn qptween/tween-x-fn
                                           :step-count 30))

                               ;; )
                               (= 17 cx) (-> s
                                             (assoc :sprite-group :outer)
                                             (qptween/add-tween
                                              (qptween/tween
                                               :pos
                                               (- (first p39) spx)
                                               :update-fn qptween/tween-x-fn
                                               :step-count 30)))
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
                                      (#{13 15} cx))
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
                    center-pos (calc-center-pos (content-pos)
                                                content
                                                text-size)
                    p10 (char-pos center-pos [10 0] text-size)
                    p29 (char-pos center-pos [29 0] text-size)]
                (update-in state [:scenes current-scene :sprites]
                           (fn [sprites]
                             (concat
                              (remove (fn [{[cx cy] :init-coords :as s}]
                                        (and (not (#{:duplicate :reference} (:sprite-group s)))
                                             (#{13 15} cx)))
                                      sprites)
                              (map (fn [cx c]
                                     (-> (hidable-text-sprite
                                          (str c)
                                          p10
                                          green
                                          qpu/default-font
                                          text-size)
                                         (assoc :init-coords [cx 0])
                                         (assoc :hidden 1)))
                                   (range 1 19)
                                   "(fn [x] (f (x x)))")
                              (map (fn [cx c]
                                     (-> (hidable-text-sprite
                                          (str c)
                                          p29
                                          green
                                          qpu/default-font
                                          text-size)
                                         (assoc :init-coords [cx 0])
                                         (assoc :hidden 1)))
                                   (range 20 38)
                                   "(fn [x] (f (x x)))"))))))))))
   

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
                                  (= green (:color s))
                                  (or (<= 1 cx 19)
                                      (<= 20 cx 37)))
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
                           
                           :else s))
                       sprites))))

   ;; merge duplicate, make 9, 10, 17 orange (f ... )
   (fn [{:keys [current-scene] :as state}]
     (-> state
         (update-in [:scenes current-scene :sprites]
                    (fn [sprites]
                      (map (fn [{[cx cy :as coords] :init-coords
                                 [r g b a] :color
                                 :as s}]
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
                               (if (= :outer (:sprite-group s))
                                 (-> s
                                     (qptween/add-tween
                                      (qptween/tween
                                       :color
                                       (- (nth orange 0) r)
                                       :update-fn (fn [c d] (update c 0 + d))
                                       :step-count 30))
                                     (qptween/add-tween
                                      (qptween/tween
                                       :color
                                       (- (nth orange 1) g)
                                       :update-fn (fn [c d] (update c 1 + d))
                                       :step-count 30))
                                     (qptween/add-tween
                                      (qptween/tween
                                       :color
                                       (- (nth orange 2) b)
                                       :update-fn (fn [c d] (update c 2 + d))
                                       :step-count 30)))
                                 s)))
                           sprites)))))

   ;; Add wrapping (f ... )
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p-6 (char-pos center-pos [-6 0] text-size)
           p-5 (char-pos center-pos [-5 0] text-size)
           p40 (char-pos center-pos [40 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (concat
                     sprites
                     (map (fn [s]
                            (qptween/add-tween
                             s
                             (qptween/tween
                              :color
                              255
                              :update-fn (fn [c d] (update c 3 + d))
                              :step-count 20)))
                          [(hidable-text-sprite "(" p-6 (assoc orange 3 0) qpu/default-font text-size)
                           (hidable-text-sprite "f" p-5 (assoc orange 3 0) qpu/default-font text-size)
                           (hidable-text-sprite ")" p40 (assoc orange 3 0) qpu/default-font text-size)]))))))

   ;; Add wrapping (f ... )
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p-9 (char-pos center-pos [-9 0] text-size)
           p-8 (char-pos center-pos [-8 0] text-size)
           p41 (char-pos center-pos [41 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (concat
                     sprites
                     (map (fn [s]
                            (qptween/add-tween
                             s
                             (qptween/tween
                              :color
                              255
                              :update-fn (fn [c d] (update c 3 + d))
                              :step-count 20)))
                          [(hidable-text-sprite "(" p-9 (assoc orange 3 0) qpu/default-font text-size)
                           (hidable-text-sprite "f" p-8 (assoc orange 3 0) qpu/default-font text-size)
                           (hidable-text-sprite ")" p41 (assoc orange 3 0) qpu/default-font text-size)]))))))

   ;; Add wrapping (f ... )
   (fn [{:keys [current-scene] :as state}]
     (let [content (get-in state [:scenes current-scene :content])
           center-pos (calc-center-pos (content-pos)
                                       content
                                       text-size)
           p-12 (char-pos center-pos [-12 0] text-size)
           p-11 (char-pos center-pos [-11 0] text-size)
           p42 (char-pos center-pos [42 0] text-size)]
       (update-in state [:scenes current-scene :sprites]
                  (fn [sprites]
                    (concat
                     sprites
                     (map (fn [s]
                            (qptween/add-tween
                             s
                             (qptween/tween
                              :color
                              255
                              :update-fn (fn [c d] (update c 3 + d))
                              :step-count 20)))
                          [(hidable-text-sprite "(" p-12 (assoc orange 3 0) qpu/default-font text-size)
                           (hidable-text-sprite "f" p-11 (assoc orange 3 0) qpu/default-font text-size)
                           (hidable-text-sprite ")" p42 (assoc orange 3 0) qpu/default-font text-size)]))))))])

(defn handle-mouse-pressed
  [{:keys [current-scene] :as state} e]
  (let [tn (get-in state [:scenes current-scene :next-transition])]
    (if (< tn (count transitions))
      (let [t-fn (nth transitions tn)]
        (-> (t-fn state)
            (update-in [:scenes current-scene :next-transition] inc)))
      (assoc-in state [:scenes current-scene] (init)))))

(defn handle-key-pressed
  [state e]
  (cond
    (= :space (:key e)) (qpscene/transition state :self-application)
    (= :r (:key e)) (do (prn "Recording? " (not (:recording? state)))
                        (update state :recording? not))
    :else state))

(defn init
  "Initialise this scene"
  []
  (let [initial-content wrapped-self-appl]
    {:sprites (sprites initial-content)
     :draw-fn draw-wrapped-self-application
     :update-fn update-wrapped-self-application
     :mouse-pressed-fns [handle-mouse-pressed]
     :key-pressed-fns [handle-key-pressed]
     :next-transition 0
     :content initial-content}))
