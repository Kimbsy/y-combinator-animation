(ns y-combinator-animation.notes)

;; set font to size big
;; (set-face-attribute 'default nil :height 500)

;; set font back to default
;; (set-face-attribute 'default nil :height 110)

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

;; Self-application function^0.5
-----------------------------------------------






    ( (fn [x] (x x))                )






-----------------------------------------------

;; Self-application function^2
-----------------------------------------------






    ( (fn [x] (x x)) (fn [x] (x x)) )






-----------------------------------------------

;; Wrapped self-application function
-----------------------------------------------






     (fn [x] (f (x x)))






-----------------------------------------------

;; Wrapped self-application function^0.5
-----------------------------------------------






   ( (fn [x] (f (x x)))                    )






-----------------------------------------------

;; Wrapped self-application function^2
-----------------------------------------------






   ( (fn [x] (f (x x))) (fn [x] (f (x x))) )






-----------------------------------------------

;; Delayed wrapped self-application function
-----------------------------------------------





             (fn [x]
               (f (fn [y]
                    ((x x) y))))





-----------------------------------------------

;; Basic `f` function structure
-----------------------------------------------




    (def f
      (fn [internal-lambda]
        (if condition?
          (internal-lambda)
          "just return some value")))




-----------------------------------------------

;; Rename `f`
-----------------------------------------------




    (def count-step
      (fn [internal-lambda]
        (if condition?
          (internal-lambda)
          "just return some value")))




-----------------------------------------------

;; Rename internal-lambda
-----------------------------------------------




    (def count-step
      (fn [recur-fn]
        (if condition?
          (recur-fn)
          "just return some value")))




-----------------------------------------------

;; Take input collection
-----------------------------------------------




    (def count-step
      (fn [recur-fn]
        (fn [coll]
          (if condition?
            (recur-fn)
            "just return some value"))))



-----------------------------------------------

;; Pass input collection recursively
-----------------------------------------------




    (def count-step
      (fn [recur-fn]
        (fn [coll]
          (if condition?
            (recur-fn coll)
            "just return some value"))))



-----------------------------------------------

;; Condition checks input
-----------------------------------------------




    (def count-step
      (fn [recur-fn]
        (fn [coll]
          (if (condition? coll)
            (recur-fn coll)
            "just return some value"))))



-----------------------------------------------

;; Count step function
-----------------------------------------------




    (def count-step
      (fn [recur-fn]
        (fn [coll]
          (if (not-empty coll)
            (inc (recur-fn (rest coll)))
            0))))



-----------------------------------------------

;; Example usage 1
-----------------------------------------------





      (def count (Y count-step))

      (count [8 4 2])





-----------------------------------------------

;; Example usage 2
-----------------------------------------------





      (def count (Y count-step))

      (count-step [8 4 2])





-----------------------------------------------

;; Example usage 3
-----------------------------------------------





      (def count (Y count-step))

      (inc
       (count-step [4 2]))




-----------------------------------------------

;; Example usage 4
-----------------------------------------------





      (def count (Y count-step))

      (inc
       (inc
        (count-step [2])))



-----------------------------------------------

;; Example usage 5
-----------------------------------------------





      (def count (Y count-step))

      (inc
       (inc
        (inc
         (count-step []))))


-----------------------------------------------

;; Example usage 6
-----------------------------------------------





      (def count (Y count-step))

      (inc
       (inc
        (inc
         0)))


-----------------------------------------------

;; Example usage 7
-----------------------------------------------





      (def count (Y count-step))

      (inc
       (inc
        1))



-----------------------------------------------

;; Example usage 8
-----------------------------------------------





      (def count (Y count-step))

      (inc
       2)




-----------------------------------------------

;; Example usage 9
-----------------------------------------------





      (def count (Y count-step))

      3





-----------------------------------------------
