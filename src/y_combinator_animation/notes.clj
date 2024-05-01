(ns y-combinator-animation.notes)

;; set font to size big
;; (set-face-attribute 'default nil :height 500)

;; set font back to default
;; (set-face-attribute 'default nil :height 115)

;; The Y Combinator
-----------------------------------------------


         ;; @TODO: add helpful comments
         (def Y
           (fn [f]
             ((fn [x]
                (x x))
              (fn [x]
                (f (fn [y]
                     ((x x) y)))))))



-----------------------------------------------

;; Calling functions
-----------------------------------------------





           (inc 42)       ;; => 43

           (inc (inc 42)) ;; => 44





-----------------------------------------------

;; Def a binding
-----------------------------------------------





           (def foo 42)

           (inc foo) ;; => 43





-----------------------------------------------

;; Lambda functions
-----------------------------------------------



   (fn [param1 param2 ...] (do stuff here))

   ;; define a +5 function
   (def plus-five (fn [n] (+ n 5)))

   (plus-five foo)          ;; => 47
   ((fn [n] (+ n 5)) foo)   ;; => 47



-----------------------------------------------

;; Self-application function
-----------------------------------------------






             (fn [x] (x x))






-----------------------------------------------

;; Wrapped self-application function
-----------------------------------------------






             (fn [x] (f (x x)))






-----------------------------------------------

;; Factorial step function
-----------------------------------------------



     (def factorial-step
       (fn [recur-fn]
         (fn [n]
           (if (= n 0)
             1
             (* n (recur-fn (- n 1)))))))




-----------------------------------------------

;; Recursive factorial function
-----------------------------------------------





      (def factorial (Y factorial-step))

      (factorial 5)     ;; => 120





-----------------------------------------------



;;







(fn [x]
  (f (fn [y]
       ((x x) y))))

(def internal-lambda
  (fn [y]
    ((x x) y)))

(def f
  (fn [internal-lambda]
    (if condition?
      (internal-lambda)
      "just return some value")))

(def f
  (fn [internal-lambda]
    (fn [ARG]
      (if condition?
        (internal-lambda <PASS IN ARG>) ; hence the y passed to (x x)
        "just return some value"))))



(def count
  (fn [recur-fn]
    (fn [coll]
      (if (empty? coll)
        0
        (+ 1 (recur-fn (rest coll)))))))
