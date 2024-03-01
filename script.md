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

So what do we do next? how about looking for an example usage that we can just copy paste?

``` Clojure
;; f = "factorial-ish"

(Y f) => g

(g 5) => 120
```

Well this is reasonably understandable, `Y` takes some function `f` and returns a new function `g` which can solve a recursive problem.

That's not super impressive until you find out that `Y` works even in languages where you don't have recursion, or iteration of any kind. No mapping, reduing, filtering, nothing. Not even for loops.



So the technical definition was a bit obtuse, and the implementation on it's own wasn't too much help. Let's try a different approach and break it down, starting from the bottom up.

## What is it used for?

> Doing recursion in languages that don’t have recursion.

When does that happen? What languages don't have recursion? Or, I don't know, other lesser forms of iterating. There's mapping, filtering, reduction, transduction... I dunno, for loops?

Well maybe you're a Mathematician working in the Lambda Calculus then you don't have access to any of these. <CLICK> How often does one do meaningful work in an abstract computational calculus? Arguably not every day.

Maybe you're writing your own language, and you're trying to do so in a purely functional way with only immutable values <CLICK>.

<CLICK>

Okay, so clearly the Y Combinator is incredibly useful and applicable in a broad set of circumstances... 

## How does it work?

There's two steps to using the Y Combinator. First we invoke it passing in a non-recursive function `f`, and it will return to us a new function `f'` which is recursive. Then we can invoke `f'` with some actual arguments and use it to solve a recursive problem.

So the Y Combinator somehow gives us recursion without using recursion. What kind of magic makes this possible? Turns out it's just good old fashioned functions, albeit some pretty abstract and difficult to think about functions.

## Clojure primer

So there's a chance that some of you haven't used Clojure before (see me afterwards, I'll get you hooked up). Here's what you need to know to follow along.

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

--------
# Part 2 - self application of self application

Okay, so back to the Y Combinator, we see there are two parts to its usage, and we want to understand what is happening in both of them.

In order to do this we're going to start from the bottom up, beginning with, quite possibly, my favourite function. The self-application function, also known as the Mockingbird.

## self application function

``` Clojure
(fn [x] (x x))
```

So we know what this is, it's an anonymous function, it takes a single parameter `x` and it calls `x` passing `x` in as an argument.

So what is `x`??? What are the allowable values?

We know it's a function because we're calling it, and that it's argument is also a function (since it is its own argument).

So we have a function that takes a single argument which is a function that takes a single argument which is a function that takes a single argument etc. etc.

Ok so this isn't actually recursion by itself, but this self-referential structure should definitely make us think that there's a possibility of recursion cropping up at some point.

So what functions could we actually use here? I guess `identity`, that's a classic, pretty boring though.

What about the self application function? It's a function that takes a single function as an argument isn't it?

### self application applied to itself

So we're considering the idea that we could pass the self application function to the self application function. What would that do? What would it look like?

- walk through how it evaluates to itself.

So what we end up with is another expression, the same expression. Now the rules of Lisp evaluation say that we can't juts stop evaluating, we need to evaluate this new expression too. Of course this will just get us back to where we started again.

We're not allowed to stop, this evaluation will keep going forever.

This is a loop, it does nothing and we can't stop it, but it's still a loop.

Two questions naturally arise. Can we get it to do something? And can we stop it?

facts               questions
- does nothing     - can we make it do something?
- can't stop it    - can we stop it?

## wrapped self application function

``` Clojure
(fn [x] (f (x x)))
```

Here's another function very similar to the previous one, but we have this extra call to `f` in there. We don't need to know what `f` is at this point, but just assume it is a function that exists.

- walk through how it evaluates to nested calls to f

Ok, so with this function we have the ability to create an infinite stack of nested calls to some function `f`.

<!-- @TODO: This example doesn't quite work, recursive functinos don't work like that -->
What kind of function wants to be called in a nested stack? A recursive one!

How about an example?

Say you are sat in a cinema some distance form the front, and you want to know what row you are in. You can ask the person in front of you what row they are in and add 1 to the response. The person in front of you can ask the person in front of them all the way to the front, when the person at the front is asked what row they are in it's obvious, so they reply "I'm in the first row". The next person says "I'm in the second row", all the way back to the person in front of you who says "I'm in the 22nd row" (I don't know how big cinemas are), therefore we are in the 23rd row.

What does this look like as an `f` function?

``` Clojure
(def f
  (fn [next-f]
    (if at-front?
      1
      (+ 1 (next-f)))))
```

<!-- @TODO: we need an interesting mid review conclusion for these self application functions, maybe do an examination of our factorial-step `f` function? -->

## Ending the loop

<!-- @TODO: describe how we'll escape the iterations?? -->

<!-- @TODO: I think we need another visual aide for this, can we show a nested tower of bubbles to represent the wrapped self application, and then a single bubble that can create a new nested bubble one at a time. -->

If you squint a bit this is kinda like the wrapped self application function, we have the call to `f` wrapping the self application of `(x x)` it just has another nested lambda inside it where normally we would just have `(f (x x))`. So what is this?

Essentially this is our escape hatch, we'll call it the delayed evaluation lambda. It's what stops the infinite loop of evaluation from being infinite.

<!-- @TODO: explain how there are two steps, one where we use Y to create a stack of f's, then another when we invoke the stack of fs (the inner fns from the factorial-step example). -->

## Back to the Y Combinator

Let's look back at the source code for the Y Combinator now that we're a bit more familiar with some of it's pieces.

The Y Combinator is a function which takes an `f`.

It then calls this lambda function on this one.

Look right here we have a function which takes an `x` and calls `x` with `x` as it's argument. The self application function!

And what are we passing into the self application function? This thing, well that's just the wrapped self application function with the delayed evaluation lambda.

So The Y Combinator takes an `f`, our iteration step function, and creates our dynamically extendable stack of nested calls.

--------
# Part 3 - putting it all together

## Actually getting stuff done

All of this is pretty difficult to think about in the abstract. Also we've been ignoring the function `f` this whole time. So let's look at an actual example `f` function and how we use it with the Y Combinator to solve a "real life" problem.

## the factorial step function

<!-- @TODO: maybe we should rename `recur-fn` to something like `next-f`? -->
``` Clojure
(def f
  (fn [next-f]
    (fn [n]
      (if (= n 0)
        1
        (* n (next-f (- n 1)))))))
```

Ok so calculating the factorial of a number isn't super exciting, but it's a well understood problem that has a simple recursive solution. Perfect for our needs.

So this function `factorial-step` is going to be our `f`. It's a function that takes a single argument `recur-fn` and returns a function that computes the current step of the iteration. If we reach a base state we can return a value, otherwise we can recurse a level deeper by calling `recur-fn`.

Now `recur-fn` is our wrapped self application function with the delayed evaluation lambda that we discussed, if we don't invoke it then the stack of nested calls to `f` can finally return a value, if we do invoke it we create an additional nested call to `f` and try again.


<!-- @TODO: this bit was crap we haven't understood the Y Combintor enough to just say `(Y factorial-step)` -->
    
We can finally invoke the Y Combinator on our `factorial-step` function. The function that this returns calculates the first step of the iteration and  contains a reference to a function that creates the next step (which contains a reference to the function that creates the _next_ step) etc. etc.

When we invoke this self-building stack of functions passing in a number `n`, we will perform steps of the factorial algorithm until we reach the base case where `n` is `0`, that step will return the number `1`, which will get returned back up the stack to be multiplied by each other step until we finally return the factorial of our inital input `n`.

``` Clojure
(def factorial (Y factorial-step))

(factorial 5)     ;; => 120
```

## Outro

I hope this has been interesting! I've really enjoyed exploring this subject and trying to present it in a way that makes it approachable.

For me the fact that we can implement recursion in an environment that doesn't have it says something really fundamental about recursion itself. It's almost like recursion already exists everywhere as some kind of universal truth and it just takes us shuffling some functions around in weird ways to reveal it.

If you're after some loosely related further reading I can recommend these books, each of which go over some aspect of what we've talked about today.
