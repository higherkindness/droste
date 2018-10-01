---
layout: docs
title: Recursion Schemes
permalink: /docs/core/recursionSchemes/
---

# Recursion Schemes

Freestyle empowers programs whose runtime can easily be overridden via implicit evidence. 

As part of its design, Freestyle is compatible with `Free` and the traditional patterns around it. Apps built with Freestyle give developers the freedom to choose automatic or manual algebras, modules, and interpreters, and intermix them as you see fit in applications based on the desired encoding.

## Implementation

Freestyle automatically generates an abstract definition of an interpreter for each one of the
algebras annotated with `@free`.
This allows you to build the proper runtime definitions for your algebras by simply extending the `Handler[M[_]]`
member in your algebras companion.

Consider the following algebra adapted to Freestyle from the [Typelevel Cats Free monads examples](http://typelevel.org/cats/datatypes/freemonad.html):

As you may have noticed, instead of implementing a Natural transformation `F ~> M`, we implement methods that closely resemble each one of the smart constructors in our `@free` algebras in Freestyle. This is not an imposition but rather a convenience as the resulting instances are still Natural Transformations.

In the example above, `KVStore.Handler[M[_]]` is already a Natural transformation of type `KVStore.Op ~> KVStoreState` in which its
`apply` function automatically delegates each step to the abstract method that you are implementing as part of the Handler.

Alternatively, if you would rather implement a natural transformation by hand, you can still do that by choosing not to implement
`KVStore.Handler[M[_]]` and providing one like so:


## Composition

Freestyle performs automatic composition of interpreters by providing the implicit machinery necessary to derive a Module interpreter
by the evidence of it's algebras' interpreters.
To illustrate interpreter composition, let's define a new algebra `Log` which we will compose with our `KVStore` operations:

Before we create a program combining all operations, let’s consider both `KVStore` and `Log` as part of a module in our application:

When `@module` is materialized, it will automatically create the `Coproduct` that matches the interpreters necessary to run the `Free` structure
below:


Once we have combined our algebras, we can evaluate them by providing implicit evidence of the Coproduct interpreters. `import freestyle.free.implicits._` brings into scope, among others, the necessary implicit definitions to derive a unified interpreter given implicit evidence of each one of the individual algebra's interpreters:


Alternatively, you can build your interpreters by hand if you choose not to use Freestyle’s implicit machinery. This can quickly grow unruly as the number of algebras increase in an application, but it’s also possible, in the spirit of providing two-way compatibility in all areas between manually built ADTs and Natural Transformations, and the ones automatically derived by Freestyle.

## Tagless Interpretation

Some imports:


Tagless final algebras are declared using the `@tagless` macro annotation.


Once your `@tagless` algebras are defined, you can start building programs that rely upon implicit evidence of those algebras
being present, for the target runtime monad you are planning to interpret to.


Note that unlike in `@free`, `F[_]` here refers to the target runtime monad. This is to provide an allocation free model where your
ops are not being reified and then interpreted. This allocation step in Free monads is what allows them to be stack-safe.
The tagless final encoding with direct style syntax is as stack-safe as the target `F[_]` you are interpreting to.

Once our `@tagless` algebras are defined, we can provide `Handler` instances in the same way we do with `@free`.


## Stack Safety

Freestyle provides two strategies to make `@tagless` encoded algebras stack safe.

### Interpreting to a stack safe monad

The handlers above are not stack safe because `Try` is not stack-safe. Luckily, we can still execute 
our program stack safe with Freestyle by interpreting to `Free[Try, ?]` instead of `Try` directly. 
This small penalty and a few extra allocations will make our programs stack safe.

We can safely invoke our program in a stack safe way, running it first to `Free[Try, ?]` then to `Try` with `Free#runTailRec`:


### Interpreting combined `@tagless` and `@free` algebras

When combining `@tagless` and `@free` algebras, we need all algebras to be considered in the final Coproduct we are interpreting to.
We can simply use tagless' `.StackSafe` representation in modules so they are considered for the final Coproduct.


Now that we've learned to define our own interpreters, let's jump into [Parallelism](../parallelism/).
