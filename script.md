## Talk Intro

Hi, I'm Dave, I'm a software engineer and amateur programming language designer. I work using Clojure for a company called Riverford Organic Farmers, so if you like lisps, functional programming or vegetables, come talk to me after.


... why am I talking about this? I was writing a toy language and needed to be able to iterate, but it was purely functional so had no mutation, our functions couldn't be defined referring to themselves. The y combinator is a solution to this, I tried it and it worked straight away. I felt uneasy that I didn't know how it was doing what it was doing, so I started trying to figure it out.


So today we're going to take a look at the Y Combinator, and hopefully we'll be able to develop some intuition as to how it works.


## Y Combinator intro

Let's start by taking a look at the definition of the Y Combinator, a pretty sensible place to start.

> Achieving unbounded recursion through fixed-point combinatorial instantiation of self-applicative lambda abstractions

Okayyy I mean that's pretty thorough, and we've got to presume technically correct. But it's not terribly enlightening.

So what do we do when the documentation goes over our head? That's right we dive blindly into the codebase to find the source in the hopes that it will all start making sense. So let's look at an implementation of the Y Combinator in Clojure.

``` Clojure
(def Y
  (fn [f]
    ((fn [x]
       (x x))
     (fn [x]
       (f (fn [y]
            ((x x) y)))))))
```

Okayyy so I see some anonymous functions, lots of anonymous functions... lots of nested anonymous functions. This isn't exactly self-documenting code here :/

@TODO: add helpful docstring

## 

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

## our factorial function

- takes an f (the next iteration)
- if we want to recur, we use f instead

``` Clojure
(def factorial
  (fn [recur-fn]
    (fn [n]
      (if (= n 0)
        1
        (* n (recur-fn (- n 1)))))))
```
