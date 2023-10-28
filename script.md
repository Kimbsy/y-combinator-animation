# Part 1 - intro and primer

## Talk Intro

Hi, I'm Dave, I'm a software engineer and amateur programming language designer. I work using Clojure for a company called Riverford Organic Farmers, so if you like lisps, functional programming or vegetables, come talk to me after.

<!-- ... why am I talking about this? I was writing a toy language and needed to be able to iterate, but it was purely functional so had no mutation, our functions couldn't be defined referring to themselves. The y combinator is a solution to this, I tried it and it worked straight away. I felt uneasy that I didn't know how it was doing what it was doing, so I started trying to figure it out. -->


So today we're going to take a look at the Y Combinator. We'll look at what it is, what problem it solves, and hopefully we'll be able to develop some intuition as to how it works.


## Y Combinator intro

What is the Y Combinator?

Let's start by taking a look at the definition of the Y Combinator, a pretty sensible place to start.

> A method for achieving unbounded recursion through fixed-point combinatorial instantiation of self-applicative lambda abstractions

Okayyy I mean that's pretty thorough, and we've got to presume technically correct. But it's not terribly enlightening.

So what do we do when the documentation goes over our head? That's right we dive blindly into the source code in the hopes that it will all start making sense. So let's look at an implementation of the Y Combinator in Clojure.

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

So the technical definition was a bit obtuse, and the implementation on it's own wasn't too much help. Let's try a different approach and break it down, starting from the bottom up.

## What is it used for?

> Doing recursion in languages that don’t have recursion.

When does that happen? What languages don't have recursion? Or, I don't know, other lesser forms of iterating. There's mapping, filtering, reduction, transduction... I dunno, for loops?

Well maybe you're working in the Lambda Calculus then you don't have access to any of these. How often does one do meaningful work in an abstract computational calculus? Arguably not every day.

Maybe you're writing your own language, and you're trying to do so in a purely functional way with only immutable values, and you've run into the problem that you can't allow functions to refer to themselves in their definition because they don't exist yet.

Okay, so clearly the Y Combinator is incredibly useful and applicable in a broad set of circumstances... 

## How does it work?

So the Y Combinator somehow gives us recursion without using recursion. What kind of magic makes this possible? Turns out it's just good old fashioned functions, albeit some pretty abstract and difficult to think about functions.

## Clojure primer

So there's a chance that some of you haven't used Clojure before (see me afterwards, I'll get you hooked up). Here's what you need to know to follow along.

### Call a function

``` Clojure
(inc 42)     ;; => 43
;; basically the same as inc(42)

(inc (inc 42))    ;; => 44
```

We call a function by wrapping it in parens along with its arguments. We can nest them however we like. The expressions get evaluated inside to out, so the internal `(inc 42)` is turned into 43 and then passed into the second inc.

### Defining a variable

``` Clojure
(def foo 42)

(inc foo)   ;; => 43
```

Pretty straightforward, we can create a variable called `foo` and give it a value 42. We can then use it anywhere you'd expect to be able to.

### Lambda functions

``` Clojure
(fn [param1 param2 ...] (do stuff here))

;; define a +5 function
(def plus-five (fn [n] (+ n 5)))

(plus-5 foo)             ;; => 47
((fn [n] (+ n 5)) foo)   ;; => 47
```

Anonymous functions have a pretty straightforward syntax. We start with `fn`, then a vector of parameters, and finally a body expression. The function will return the result of evaluating the body.

We can bind them to names and use them like variables, or we can use the lambda expression directly in place of a function name.

--------
# Part 2 - self application of self application

Okay, so back to the Y Combinator. We wanted to look at some weird functions.

To get started we're going to look at a delightful little function, one of my favourites, the self-application function.

## self application function

``` Clojure
(fn [x] (x x))
```

So this is a lambda function, it's small and straightforward but has some interesting subtlety to it. It takes a parameter `x` and returns the result of calling `x` as a function, passing in `x` as its argument.

So what can `x` be? We know we're going to call it, so `x` has to be a function, we also know that `x` takes a single argument, and that argument is itself. So `x` must be a function that takes a single function as its argument.

- do we want to go through the identity example?

### self application applied to itself

Now what if the function that we pass into the self-application function, what if this `x`, was the self-application function. What would that do? What would it look like?

- walk through how it evaluates to itself.
- not allowed to stop, keeps going for ever, will crash your computer.
- this is a loop, an infinite one, but still a loop.
- can we inject work into the loop? can we escape?


## wrapped self application function

``` Clojure
(fn [x] (f (x x)))
```

- similar to self application but wraps body in call to f
- walk through how it evaluates to nested calls to f
- we have the ability to create an infinite sequence of nested calls to a function.
- how can we write a function that wants this?

## That extra lambda there, what does that do?

- explain the delayed evaluation by wrapping in a lambda allows the evaluation to complete, giving us a function we can call at runtime.

--------
# Part 3 - putting it all together

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

## Outro

- thanks, hopefully helpful
- resources to look at
- feel free to talk to me
- questions?
