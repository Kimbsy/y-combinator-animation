(ns y-combinator-animation.core
  (:gen-class)
  (:require [quip.core :as qp]
            [y-combinator-animation.scenes.self-application :as self-application]
            [y-combinator-animation.scenes.wrapped-self-application :as wrapped-self-application]))

(defn setup
  "The initial state of the game"
  []
  {:recording? false})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:self-application (self-application/init)
   :wrapped-self-application (wrapped-self-application/init)})

;; Configure the game
(def y-combinator-animation-game
  (qp/game {:title          "y-combinator-animation"
            :size           [1600 900]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :self-application}))

(defn -main
  "Run the game"
  [& args]
  (qp/run y-combinator-animation-game))


(def f
  (fn [next-f]
    (if at-front?
      1
      (+ 1 (next-f)))))



(def count
  (fn [next-fn]
    (fn [coll]
      (if (= () coll)
        0
        (+ 1 (next-fn (rest coll)))))))


(def fact
  (fn [n]
    (if (= 0 n)
      1
      (* n (fact (- n 1))))))

(def fact
  (fn [recur-fn]
    (fn [n]
      (if (= 0 n)
        1
        (* n (recur-fn (- n 1)))))))
