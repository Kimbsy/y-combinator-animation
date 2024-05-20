(ns y-combinator-animation.core
  (:gen-class)
  (:require [quip.core :as qp]
            [y-combinator-animation.scenes.self-application :as self-application]
            [y-combinator-animation.scenes.wrapped-self-application :as wrapped-self-application]
            [y-combinator-animation.scenes.circles :as circles]))

(defn setup
  []
  {:recording? false
   :variant :a
   :delayed-animation-cycles 0})

(defn init-scenes
  []
  {:self-application (self-application/init)
   :wrapped-self-application (wrapped-self-application/init)
   :circles (circles/init)})

(def y-combinator-animation-game
  (let [size 0.9]
    (qp/game {:title          "y-combinator-animation"
              :size           [(* 1920 size) (* 1080 size)] ; projector is 1920x1080
              :setup          setup
              :init-scenes-fn init-scenes
              :current-scene  :self-application})))

(defn -main
  [& args]
  (qp/run y-combinator-animation-game))
