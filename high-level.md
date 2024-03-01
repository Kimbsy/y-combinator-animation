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






## Act 1

- who am I?
- what is the Y Combinator?
- definition
- source code
- example usage
- derive what it is used for
- Clojure primer

## Act 2

- self application^2 <animation>
- wrapped self-application^2 <animation>
- what is f?

## Act 3

- look at Y again, break it down

- delayed evaluation lambda, explain how there are two steps, one where we use Y to create a stack of f's, then another when we invoke the stack of fs (the inner fns from the factorial-step example).

- use Y to create f chain <animation>?
- use f chain to solve problems <animation>?
- outro, reading
