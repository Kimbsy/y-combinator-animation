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

Well maybe you're a Mathematician working in the Lambda Calculus then you don't have access to any of these. <CLICK> How often does one do meaningful work in an abstract computational calculus? Arguably not every day.

Maybe you're writing your own language, and you're trying to do so in a purely functional way with only immutable values <CLICK>.

<CLICK>

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

We call a function by wrapping it in parens along with its arguments. We can nest them however we like. The expressions get evaluated inside to out, so in the second example here the internal `(inc 42)` is turned into 43 and then passed into the second inc which gives us 44.

### Defining a variable

``` Clojure
(def foo 42)

(inc foo)   ;; => 43
```

Pretty straightforward, we can create a variable called `foo` and give it a value 42. We can then use it anywhere you'd expect to be able to.

### Lambda functions

;; @TODO: fix plus-5 plus-five

``` Clojure
(fn [param1 param2 ...] (stuff here))

;; define a +5 function
(def plus-five (fn [n] (+ n 5)))

(plus-5 foo)             ;; => 47
((fn [n] (+ n 5)) foo)   ;; => 47
```

Anonymous functions have a pretty straightforward syntax. We start with `fn`, then a vector of parameters, and finally a body expression. The function will return the result of evaluating the body.

We can give lambdas names with `def` and use them like variables, or we can use the lambda expression directly in place of a function name.

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

Can anyone think of a function that takes a single function as it's argument? What about the self application function? It's a function and it takes a single argument `x`, which as we discussed must itself be a function.

### self application applied to itself

So we're considering the idea that we could pass the self application function to the self application function. What would that do? What would it look like?

- walk through how it evaluates to itself.

So what we end up with is another expression, the same expression. Now the rules of Lisp evaluation say that we can't juts stop evaluating, we need to evaluate this new expression too. Of course this will just get us back to where we started again.

We're not allowed to stop, this evaluation will keep going forever.

This is a loop, it does nothing and we can't stop it, but it's still a loop.

Two questions naturally arise. Can we somehow inject work into each iteration of this loop? And can we escape once enough work has been done?

## wrapped self application function

``` Clojure
(fn [x] (f (x x)))
```

 <!-- @TODO: cut this way down -->
Here's another function which we'll call the wrapped self application function. It's very similar to the standard self application function, but we have this extra call to some function `f` that encloses the self application bit. We don't need to know what `f` is at this point, but just assume it is a function that exists (pretty low bar).

- walk through how it evaluates to nested calls to f

Ok, so with this function we have the ability to create an infinite stack of nested calls to some function `f`.

<!-- @TODO: can we give an example of how we cold use an inifinte stakc of f's? -->

## Ending the loop

<!-- @TODO: describe how we'll end things?? -->

## Back to the Y Combinator

Let's look back at the source code for the Y Combinator now that we're a bit more familiar with some of it's pieces.

The Y Combinator is a function which takes an `f`.

It then calls this lambda function on this one.

Look right here we have a function which takes an `x` and calls `x` with `x` as it's argument. The self application function!

And what are we passing into the self application function? This thing.

If you squint a bit this is kinda like the wrapped self application function, we have the call to `f` wrapping the self application of `(x x)` it just has another nested lambda inside it where normally we would just have `(f (x x))`. So what is this?

Essentially this is our escape hatch, we'll call it the delayed evaluation lambda. It's what stops the infinite loop of evaluation from being infinite.

 <!-- @TODO: clean this explanation up, add slides -->

(x x) return a function of one argument, so if we wanted to invoke it we would have something like ((x x) foo). we can abstract this with a lambda (fn [y] ((x x) y)) This is a function that takes one argument y and passes it to the function created by self application of x.

The key difference is _when_ the function returned by (x x) is created. In the simple case it is calculated during the top level expression evaluation, whereas in the second case it is only created when the (fn [y]) lambda is invoked.

instead of our expression evaluating to an infinite stack, it evaluated to a single iteration which has a function that creates the next iteration. but he important part it that it doesn't _have_ to create the next step.

--------
# Part 3 - putting it all together

## Actually getting stuff done

All of this is pretty difficult to think about in the abstract. Also we've been ignoring the function `f` this whole time. So let's look at an actual example `f` function and how we use it with the Y Combinator to solve a "real life" problem.

## the factorial step function

``` Clojure
(def factorial-step
  (fn [recur-fn]
    (fn [n]
      (if (= n 0)
        1
        (* n (recur-fn (- n 1)))))))
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

<!-- @TODO: better summation, talk about small but complex, building up hard to hold abstact concepts, ending with usable thing -->

<!-- @TODO: as they say in the forge, the tao that can be named is not the eternal tao, someone's description of enlightenment is not enlightenment. if this felt close to interesting, you sould have a play with it yourself, when it clicks its staggeringly beautiful -->

I hope this has been interesting! I've really enjoyed exploring this subject and trying to present it in a way that makes it a bit more approachable.

I find it fundamentally delightful that the sel f application of self application, a concept which seems so abstract at first turns out the be the lynch pin of a surprisingly usable method of iteration.

If this kind of stuff interests you I can recommend the following books
