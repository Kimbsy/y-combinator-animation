(ns y-combinator-animation.scenes.sim
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]
            [clojure.string :as s]
            [y-combinator-animation.common :as common]))

;; @TODO: need ot figure out a nice way of describing a transition

;; want to be able to step forward and backwards

;; magic variable for pleasant vertical line spacing
(def dy 1.3)

(defn update-compound-sprite
  [compound-sprite]
  (update compound-sprite :chars (partial map (fn [s]
                                                ((:update-fn s) s)))))

(defn draw-compound-sprite
  [{:keys [children]}]  
  (doall
   (map (fn [s]
          ((:draw-fn s) s))
        children)))

(defn calc-center-pos
  [pos content size]
  (let [lines (s/split-lines content)
        y-offset (* (count lines) size dy 0.5)
        max-length (apply max (map count lines))
        x-offset (* max-length size 0.5 0.5)]
    (map - pos [x-offset y-offset])))

(defn init-chars
  [parent-pos content color size font]
  (let [[px py] (calc-center-pos parent-pos content size)
        lines (s/split-lines content)]
    (apply concat
           (map-indexed
            (fn [i l]
              (keep-indexed
               (fn [j c]
                 (when (not= \space c)
                   (qpsprite/text-sprite
                    (str c)
                    [(+ px (* j size 0.5))
                     (+ py (* i size dy))]
                    :color color
                    :font font)))
               l))
            lines))))

(defn compound-text-sprite
  "Create a compound sprite for displaying text in a monospaced
  font.

  Gives us the ability to control the way each character/substring is
  displayed to allow fancy animations."
  [content pos & {:keys [color
                         size]
                  :or {color common/white
                       size qpu/default-text-size}}]
  {:sprite-group :compound-text
   :uuid         (java.util.UUID/randomUUID)
   :update-fn update-compound-sprite
   :draw-fn draw-compound-sprite
   :color color
   :size size
   :content content
   :pos (calc-center-pos pos content size)
   :children (init-chars pos content color size qpu/default-font)})

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(compound-text-sprite
    "(def Y (fn [f]
         ((fn [x]
            (x x))
          (fn [x]
            (f (x x))))))"
    [(/ (q/width) 2) (/ (q/height) 2)])])

(defn draw-sim
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/grey)

  ;; draw cross at mouse pos for alignment debugging
  (q/stroke 255)
  (q/line 0 (q/mouse-y) (q/width) (q/mouse-y))
  (q/line (q/mouse-x) 0 (q/mouse-x) (q/height))
  
  (qpsprite/draw-scene-sprites state))

(defn remove-completed-tweens
  [sprites]
  (map (fn [s]
         (if ((qpsprite/group-pred :compound-text) s)
           (update s :children (fn [children]
                                 (map (fn [child]
                                        (update child :tweens #(remove :completed? %)))
                                      children)))
           s))
       sprites))

(defn update-compound-sprite-tweens
  [{:keys [current-scene] :as state}]
  (let [compound-sprites (filter (qpsprite/group-pred :compound-text)
                                (get-in state [:scenes current-scene :sprites]))
        single-sprites  (remove (qpsprite/group-pred :compound-text)
                                (get-in state [:scenes current-scene :sprites]))
        updated-sprites (concat single-sprites
                                (map (fn [compound-sprite]
                                       (update compound-sprite
                                               :children
                                               (fn [children]
                                                 (transduce (comp (map qptween/update-sprite)
                                                                  (map qptween/handle-on-yoyos)
                                                                  (map qptween/handle-on-repeats)
                                                                  (map qptween/handle-on-completes))
                                                            conj
                                                            children))))
                                     compound-sprites))
        cleaned-sprites (remove-completed-tweens updated-sprites)]
    (assoc-in state [:scenes current-scene :sprites]
              cleaned-sprites)))

(defn update-sim
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites
      qptween/update-sprite-tweens
      update-compound-sprite-tweens))

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

(defn handle-mouse-pressed
  [{:keys [current-scene] :as state} e]
  (update-in state [:scenes current-scene :sprites]
             (fn [sprites]
               (map (fn [sprite]
                      (if ((qpsprite/group-pred :compound-text) sprite)
                        (update sprite :children
                                (fn [children]
                                  (map (fn [child]
                                         (-> child
                                             (qptween/add-tween (random-tween-x))
                                             (qptween/add-tween (random-tween-y))))
                                       children)))
                        sprite))
                    sprites))))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-sim
   :update-fn update-sim
   :mouse-pressed-fns [handle-mouse-pressed]})
