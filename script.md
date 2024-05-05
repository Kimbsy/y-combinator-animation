# Part 1 - intro and primer

## Talk Intro

Hi, I'm Dave, I'm a software engineer and amateur programming language designer. I work using Clojure for a company called Riverford Organic Farmers, so if you like lisps, functional programming or vegetables, come talk to me after.

<!-- ... why am I talking about this? I was writing a toy language and needed to be able to iterate, but it was purely functional so had no mutation, our functions couldn't be defined referring to themselves. The y combinator is a solution to this, I tried it and it worked straight away. I felt uneasy that I didn't know how it was doing what it was doing, so I started trying to figure it out. -->


So today we're going to take a look at the Y Combinator. We'll look at what it is, what problem it solves, and hopefully we'll be able to develop some intuition as to how it works.

## Y Combinator intro

What is the Y Combinator?

Let's start by taking a look at the definition of the Y Combinator, a pretty sensible place to start.

> A method for achieving unbounded recursion through fixed-point combinatorial instantiation of self-applicative lambda abstractions

So I mean that's pretty thorough, and we've got to presume technically correct. But it's not terribly enlightening.

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

Wow. Okay. So I see some anonymous functions, lots of nested anonymous functions. This isn't exactly self-documenting code here :/

So what do we do next? how about looking for an example usage that we can just copy paste?

``` Clojure
;; f = "factorial-ish"

(Y f) => g

(g 5) => 120
```

Well this is reasonably understandable, there's some function f which is factorialish, `Y` takes this function `f` and returns a new function `g` which can solve a recursive problem.

That might not be super impressive until you find out that `Y` works even in languages where you don't have recursion, or iteration of any kind. No mapping, reducing, filtering, nothing. Not even for loops.

So that's what it's for:

> Doing recursion in languages that don’t have recursion.

When does that happen? What languages don't have recursion? 

Well maybe you're a Mathematician working in the Lambda Calculus. <CLICK> How often does one do meaningful work in an abstract computational calculus? Arguably not every day.

Maybe you're writing your own language, and you're trying to do so in a purely functional way with only immutable values <CLICK>.

<CLICK>

Okay, so clearly the Y Combinator is incredibly useful and applicable in a broad set of circumstances... 


<!-- @TODO: maybe something about beauty or elegance as a reason for doing this??? give grumpy techincal people reason to care -->



^^^^^^^^^^^^^ GOOD ^^^^^^^^^^^^^^



## Clojure primer

So there's a chance that some of you haven't used Clojure before (see me afterwards, I'll get you hooked up). Dont' worry, Here's what you need to know to follow along.

### Call a function

``` Clojure
(inc 42)     ;; => 43
;; basically the same as inc(42)

(inc (inc 42))    ;; => 44
```

We call a function by wrapping it in parens along with its arguments. We can nest them however we like. The expressions get evaluated inside to out, so in the second example here the internal `(inc 42)` is turned into 43 and then passed into the second inc which gives us 44.

### Defining a variable

``` Clojure
(def foo 42)

(inc foo)   ;; => 43
```

Pretty straightforward, we can create a variable called `foo` and give it a value 42. We can then use it anywhere you'd expect to be able to.

### Lambda functions

``` Clojure
(fn [param1 param2 ...] (stuff here))

;; define a +5 function
(def plus-five (fn [n] (+ n 5)))

(plus-five foo)          ;; => 47
((fn [n] (+ n 5)) foo)   ;; => 47
```

Anonymous functions have a pretty straightforward syntax. We start with `fn`, then a vector of parameters, and finally a body expression. The function will return the result of evaluating the body.

We can give lambdas names with `def` and use them like variables, or we can use the lambda expression directly in place of a function name.


<!-- @TODO: maybe write out the function as we describe it in animations maybe? maybe not? work on this -->


^^^^^^ PRIMER IS FINE, COULD USE FINESSING ^^^^^








--------
# part 2 - self application of self application

So here we are back at the Y Combinator, let's take a look at it's structure.

We're defining a function Y which takes a function `f` as it's argument.

It then calls this lambda function on this one.

Let's take a look at that first lambda, which we'll call the self-application function.

## self application function

``` Clojure
(fn [x] (x x))
```

So this seems pretty straightforward, it's an anonymous function, it has a single parameter `x` and it calls `x` passing `x` in as an argument.

So what is `x`??? What are the allowable values?

We know it's a function because we're calling it, and that it's argument is also a function (since it is its own argument).

So we have a function that takes a single argument which is a function that takes a single argument which is a function that takes a single argument etc. etc.

Ok so this isn't actually recursion by itself, but this psuedo-self-referential structure should definitely make us think that there's a possibility of recursion cropping up at some point.

So what functions could we actually use here? I guess `identity`, that's a classic, pretty boring though.

What about the self application function itself? It's a function that takes a single function as an argument? What would that do? What would it look like?

### self application applied to itself

> DEMO or video, walk through how it evaluates to itself

So what we end up with is another expression, the same expression. Now the rules of Lisp evaluation say that we can't juts stop evaluating, we need to evaluate this new expression too, but if we do that we end up in exactly the same position. We're not allowed to stop, this evaluation will keep going forever.

So what we have here is a loop, it does nothing and we can't stop it, but it's still a loop.

Two questions naturally arise. Can we get it to do something? And can we stop it?

facts               questions
- does nothing     - can we make it do something?
- can't stop it    - can we stop it?

## wrapped self application function

``` Clojure
(fn [x] (f (x x)))
```

In order to start answering those questions we're going to jump back to the Y combinator and look at our second lambda here.

Now this is a little more complex than we need it to be for now, so we're going to look at a simpler version first and we'll come back to this one after.

This function is very similar to the previous self-application function, but we have this extra call to `f` in there. We don't need to know what `f` is at this point, but just assume it is a function that exists.

> DEMO or video, walk through how it evaluates to nested calls to f

Ok, so with this function we have the ability to create an infinite stack of nested calls to some function `f`.

In essence we've managed to inject some work into each iteration of the infinite evaluation loop. Nice! Our infinite evaluation loop can now perform some kind of work.



^^^^^^ PRETTY GOOD, NEED TO GET FEEDBACK ^^^^^


--------
# Part 3 - final pieces

## delayed evaluation lambda

<!-- ;; @TODO: need to do slides for this -->

So can we stop it? yep.

Let's look at that more complicated version of the nested f function again.

``` Clojure
(fn [x]
  (f (fn [y]
       ((x x) y))))
```

So what happens if we apply this function to itself? It will invoke `f` applied to this internal lambda, and this lambda is ready to apply the `x` to the `x`, but crucially, hasn't done it yet.

So `f` is going to be passed this lambda, and if it decides to invoke it it will execute the `(x x)` letting us go one layer deeper into the infinite evaluation loop, and in doing so creating another nested call to `f` which can make the same choice. Each layer has the ability to create the next layer if it wants to.

<!-- ;; @TODO: make this -->
> DEMO or video, high level simulation of creating a chain of bubbles which dynamically decide whether to extend the chain

## what the f?

So with that in hand we can start to think about what the `f` this function actually is.

``` Clojure
(def f
  (fn [internal-lambda]
    (if condition?
      (internal-lambda)
      "just return some value")))
```

Here's a function which takes that internal lambda as an argument, and if some condition is met it can choose to invoke that lambda which will invoke `f` again, giving it the same choice. Alternatively it can just return a value and stop the evaluation loop.

Now this is close, but it's not quite what we want, we want to solve recursive problems, so we will have some input value that we want to pass in somewhere.  We want our `condition?` function to be checking our input value, and we want the input value to change each iteration so the condition eventually flips and we return a value instead of always going a level deeper.

<!-- ;; @TODO: need to describe this counting function, talk about how we're injecting the args, e.g. how our recur-fn takes an argument -->
Let's look at a real life problem that we can solve recursively, counting. Specifically counting the number of elements in a collection.


``` Clojure
(def count-step
  (fn [recur-fn]
    (fn [coll]
      (if (empty? coll)
        0
        (+ 1 (recur-fn (rest coll)))))))
```

<!-- ;; @TODO: make this -->
> DEMO or video, go through actual example where we have a coll

Okay, we're ready to do this.

``` Clojure
(def count (Y count-step))

(count [0 0 0])     ;; => 3
```

We can invoke the Y Combinator on our `count-step` function. The function that this returns contains a reference to a function that creates the next step (which contains a reference to the function that creates the _next_ step) etc. etc.

When we invoke this self-building stack of functions passing in a collection `coll`, we will invoke our `recur-fn`, passing in a smaller collection each time until we reach the base case where `coll` is empty. That iteration will return the number `0`, which will get returned back up the stack to be incremented by each other step until we finally return the count of elements in our original collection `coll`.












## Outro

<!-- ;; @TODO: this need slides, they're impactful statements, maybe conclusion??? -->

I hope this has been interesting! I've really enjoyed exploring this subject and trying to present it in a way that makes it approachable.

What have we covered?

- self application
- infinite evaluation loops
- delayed evaluation
- non-recursive recursive functions

For me the fact that we can implement recursion in an environment that doesn't have it says something really fundamental about recursion itself. It's almost like recursion already exists everywhere as some kind of universal truth and it just takes us shuffling some functions around in weird ways to reveal it.

<alt> Recursion exists as a fundamental universal truth, you need only the heart to find it

If you're after some loosely related further reading I can recommend these books, each of which go over some aspect of what we've talked about today.
