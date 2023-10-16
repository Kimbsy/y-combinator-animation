## Talk Intro

Hi, I'm Dave, I'm a software engineer and amateur programming language designer. I work using Clojure for a company called Riverford Organic Farmers, so if you like lisps, functional programming or vegetables, come talk to me after.


... why am I talking about this? I was writing a toy language and needed to be able to iterate, but it was purely functional so had no mutation, our functions couldn't be defined referring to themselves. The y combinator is a solution to this, I tried it and it worked straight away. I felt uneasy that I didn't know how it was doing what it was doing, so I started trying to figure it out.


So today we're going to take a look at the Y Combinator, and hopefully we'll be able to develop some intuition as to how it works.


## Y Combinator intro

Let's start by taking a look at the definition of the Y Combinator, a pretty sensible place to start.

> Achieving unbounded recursion through fixed-point combinatorial instantiation of self-applicative lambda abstractions

:DONE

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

## Clojure primer

So there's a chance that some of you haven't used Clojure before (see me afterwards, I'll get you hooked up)
