(ns y-combinator-animation.core
  (:gen-class)
  (:require [quip.core :as qp]
            [y-combinator-animation.scenes.sim :as sim]))

(defn setup
  "The initial state of the game"
  []
  {})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:sim (sim/init)})

;; Configure the game
(def y-combinator-animation-game
  (qp/game {:title          "y-combinator-animation"
            :size           [1600 900]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :sim}))

(defn -main
  "Run the game"
  [& args]
  (qp/run y-combinator-animation-game))
