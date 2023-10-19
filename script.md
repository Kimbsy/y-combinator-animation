# Part 1

## Talk Intro

Hi, I'm Dave, I'm a software engineer and amateur programming language designer. I work using Clojure for a company called Riverford Organic Farmers, so if you like lisps, functional programming or vegetables, come talk to me after.

<!-- ... why am I talking about this? I was writing a toy language and needed to be able to iterate, but it was purely functional so had no mutation, our functions couldn't be defined referring to themselves. The y combinator is a solution to this, I tried it and it worked straight away. I felt uneasy that I didn't know how it was doing what it was doing, so I started trying to figure it out. -->


So today we're going to take a look at the Y Combinator. We'll look at what it is, what problem it solves, and hopefully we'll be able to develop some intuition as to how it works.


## Y Combinator intro

Let's start by taking a look at the definition of the Y Combinator, a pretty sensible place to start.

> Achieving unbounded recursion through fixed-point combinatorial instantiation of self-applicative lambda abstractions

Okayyy I mean that's pretty thorough, and we've got to presume technically correct. But it's not terribly enlightening.

So what do we do when the documentation goes over our head? That's right we dive blindly into the codebase to find the source in the hopes that it will all start making sense. So let's look at an implementation of the Y Combinator in Clojure.

``` Clojure
;; @TODO: add helpful docstring
(def Y
  (fn [f]
    ((fn [x]
       (x x))
     (fn [x]
       (f (fn [y]
            ((x x) y)))))))
```

Okayyy so I see some anonymous functions, lots of anonymous functions... lots of nested anonymous functions. This isn't exactly self-documenting code here :/

## Clojure primer

So there's a chance that some of you haven't used Clojure before (see me afterwards, I'll get you hooked up). Here's what you need to know to follow along.

### Call a function

``` Clojure
(inc 42)     ;; => 43
;; basically the same as inc(42)

(inc (inc 42))    ;; => 44
```

We call a function by wrapping it in parens along with its arguments. We can nest them however we like.

### Def a binding

``` Clojure
(def foo 42)

(inc foo)   ;; => 43
```

Pretty straightforward, we can create a variable called `foo` and give it a value 42.

### Lambda functions

``` Clojure
;; define a +5 function
(def plus-five (fn [n] (+ n 5))

((fn [n] (+ n 5)) foo)   ;; => 47
```

Anonymous functions have a pretty straightforward syntax. We can use them in place of named functions.

--------
# Part 2

Okay, so back to the Y Combinator. The technical definition was a bit obtuse, and the implementation on it's own wasn't too much help. Let's break it down, starting from the bottom up.

## What is it used for?

> Doing recursion in languages that don’t have recursion.

When does that happen? What languages don't have recursion? Or, I don't know, other lesser forms of iterating. There's mapping, filtering, reduction, transduction... I dunno, for loops?

Well if you're working in the Lambda Calculus then you don't have access to any of these. How often does one work with a purely mathematical computational calculus? Arguably not every day.

If you're writing your own language, and you're trying to do so in a pure functional way with only immutable values, than you'll find implementing recursion to be pretty tricky.

Okay, so clearly the Y Combinator is incredibly useful and applicable in a broad set of circumstances... 

## How does it work?

So the Y Combinator somehow gives us recursion without using recursion. What kind of magic allows this? Turns out it's just good old fashioned functions, albeit some pretty abstract and difficult to think about functions.

To get started we want to look at a delightful little function, the self-application function.

## self application function

``` Clojure
(fn [x] (x x))
```

- what does this do? what can x be? a function. a function that takes a single function. identity? 

## self application applied to itself

- what if x were ... the self application function?
- walk through how it evaluates to itself.
- not allowed to stop, keeps going for ever, will crash your computer.
- this is a loop, an infinite one, but still a loop.
- can we inject work into the loop? can we escape?


## wrapped self application function

- similar to self application but wraps body in call to f
- walk through how it evaluates to nested calls to f
- we have the ability to create an infinite sequence of nested calls to a function.
- how can we write a function that wants this?

--------
# Part 3

## Actually getting stuff done

<!-- what does a real function f look like, let's do an example -->

## the factorial step function

- takes a recur-fn (the next iteration)
- returns a function which is a closure over the recur-fn
- this closure implements one step of our algorithm
- if we want to end, we return a value, if we want to recur, we use recur-fn instead

``` Clojure
(def factorial-step
  (fn [recur-fn]
    (fn [n]
      (if (= n 0)
        1
        (* n (recur-fn (- n 1)))))))
```

We then invoke the Y Combinator on our factorial function. The function that this returns is a closure over a closure over a closure ad infinitum. When these iteration steps are evaluated each gets the chance to return a value and end the evaluation, or dive one level deeper.

``` Clojure
(def factorial (Y factorial-step))

(factorial 5)     ;; => 120
```
