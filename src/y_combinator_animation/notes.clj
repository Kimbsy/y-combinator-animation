(ns y-combinator-animation.notes)

;; Achieving unbounded recursion through fixed-point combinatorial instantiation of self-applicative lambda abstractions

(def Y
  (fn [f]
    ((fn [x]
       (x x))
     (fn [x]
       (f (fn [y]
            ((x x) y)))))))

(def factorial
  (fn [do-recur]
    (fn [n]
      (if (= n 0)
        1
        (* n (do-recur (- n 1)))))))

((Y factorial) 5)
;; => 120
