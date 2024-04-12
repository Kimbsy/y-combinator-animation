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

Ok so this isn't actually recursion by itself, but this self-referential structure should definitely make us think that there's a possibility of recursion cropping up at some point.

So what functions could we actually use here? I guess `identity`, that's a classic, pretty boring though.

What about the self application function itself? It's a function that takes a single function as an argument? What would that do? What would it look like?

### self application applied to itself

> DEMO or video, walk through how it evaluates to itself.

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

- walk through how it evaluates to nested calls to f

Ok, so with this function we have the ability to create an infinite stack of nested calls to some function `f`.

In essence we've managed to inject some work into each iteration of the infinite evaluation loop. Nice! Our infinite evaluation loop can now perform some kind of work.



^^^^^^ PRETTY GOOD, NEED TO GET FEEDBACK ^^^^^





VVVVVV BAD VVVVVVV


<!-- @TODO: This example doesn't quite work, recursive functinos don't work like that -->
What kind of function wants to be called in a nested stack? A recursive one!


<!-- How about an example? -->

<!-- Say you are sat in a cinema some distance form the front, and you want to know what row you are in. You can ask the person in front of you what row they are in and add 1 to the response. The person in front of you can ask the person in front of them all the way to the front, when the person at the front is asked what row they are in it's obvious, so they reply "I'm in the eroth row". The next person says "I'm in the first row", all the way back to the person in front of you who says "I'm in the 22nd row" (I don't know how big cinemas are), therefore we are in the 23rd row. -->

<!-- What does this look like as an `f` function? -->

``` Clojure
 (def f 
   (fn [next-f]
     (if at-front?
       0
       (+ 1 (next-f)))))
 ```






;; @TODO: maybe use the normal fact function compared to the Y combinator fact function?

(def fact
  (fn [n]
    (if (= 0 n)
      1
      (* n (fact (- n 1))))))

(def fact
  (fn [recur-fn]
    (fn [n]
      (if (= 0 n)
        1
        (* n (recur-fn (- n 1)))))))








<!-- @TODO: we need an interesting mid review conclusion for these self application functions, maybe do an examination of our factorial-step `f` function? -->

## Ending the loop

<!-- @TODO: describe how we'll escape the iterations?? -->

<!-- @TODO: I think we need another visual aide for this, can we show a nested tower of bubbles to represent the wrapped self application, and then a single bubble that can create a new nested bubble one at a time. -->

<!-- @TODO: explain how there are two steps, one where we use Y to create a stack of f's, then another when we invoke the stack of fs (the inner fns from the factorial-step example). -->

So The Y Combinator takes an `f`, our iteration step function, and creates a dynamically extending stack of nested calls.










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

;; @TODO: this need slides, they're impactful statements, maybe conclusion???

;; @TODO funny meme? though honestly maybe not

- IF I HAD ONE
- butterfly, is this ... recursion?
- distracted boyfriend?
- we have recursion at home

I hope this has been interesting! I've really enjoyed exploring this subject and trying to present it in a way that makes it approachable.

What have we covered?

- self application
- infinite evaluation loops
- delayed evaluation
- non-recursive recursive functions

For me the fact that we can implement recursion in an environment that doesn't have it says something really fundamental about recursion itself. It's almost like recursion already exists everywhere as some kind of universal truth and it just takes us shuffling some functions around in weird ways to reveal it.

<alt> Recursion exists as a fundamental universal truth, you need only the heart to find it

If you're after some loosely related further reading I can recommend these books, each of which go over some aspect of what we've talked about today.
