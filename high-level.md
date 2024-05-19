# High level talk plan

## Goals

The audience will come away with:

- A comprehensive understanding of the inner workings of the Y Combinator
- An intuitive understanding of the Y Combinator at a high level
- Working examples of how to solve recursive problems using the Y Combinator

## Audience

This talk should appeal to people who:

- Have never heard of the Y Combinator before
- Have heard of it but not looked into it
- Have looked into it and found it tough to figure out
- Understand it well and want to feel smart about themselves?

An understanding of functional programming is assumed. Experience with (or expose to) Clojure or other Lisps is helpful, but not required.


## Part 1 - intro and primer

- who am I?
- what is the Y Combinator?
- definition
- source code
- example usage
- what it is used for
- Clojure primer

## part 2 - self application of self application

- back to Y combiantor
- self application^2
- <animation>
- back to Y combinator
- wrapped self-application^2
- <animation>

## Part 3 - final pieces

- delayed evaluation lambda

- what is f?
- use f chain to solve problems
- <animation>?
- outro, reading


# TODO:

- @TODO: THIS ONE NEXT make the delayed circle example animate slower, especially on the collapse

- practice
- practice with second screen, presentation mode, tabbing out to animations

- can you send fullscreeen to second display in quil???? I think so, just `:display 1` in sketch setup? does this work with quip? no, would need to override `quip.core/run`. Also would need to make all text adjust to screen size.
- make animations dependant on screen size
- re-record demo videos (use real software, not image dumping)
- add button/link to demo slides to watch them in another tab
- delete current (legacy) video slides
- @DONE sent email asking about projector resolution

- submit questions:
  - why have you chosen to do this in clojure? lisp is a AST, evluationin place is very natural, also I love clojure come see me after.
  - why have you chosen to do this at all? I wrote my first lisp, I thought it would be fun to use it to write another lisp, byt now I had no state or mutation, Y combinatoor only option. I was annoyed that it worked firs try without me needing to understand it.


# timings
- @DONE currently 14 minutes, need to slow down, pause for jokes
- 19:07
