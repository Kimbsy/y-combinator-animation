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

   (plus-5 foo)             ;; => 47
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
