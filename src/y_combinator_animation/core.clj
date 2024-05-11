(ns y-combinator-animation.core
  (:gen-class)
  (:require [quip.core :as qp]
            [y-combinator-animation.scenes.self-application :as self-application]
            [y-combinator-animation.scenes.wrapped-self-application :as wrapped-self-application]
            [y-combinator-animation.scenes.circles :as circles]))

(defn setup
  []
  {:recording? false
   :variant :a})

(defn init-scenes
  []
  {:self-application (self-application/init)
   :wrapped-self-application (wrapped-self-application/init)
   :circles (circles/init)})

(def y-combinator-animation-game
  (qp/game {:title          "y-combinator-animation"
            :size           :fullscreen
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :self-application}))

(defn -main
  [& args]
  (qp/run y-combinator-animation-game))
