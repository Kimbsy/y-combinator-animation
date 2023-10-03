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
