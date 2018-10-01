---
layout: docs
title: Data Structures
permalink: /docs/core/dataStructures/
---

# Data Structures

Algebraic Data Types are the foundation used to define `Free` based applications and libraries that express their operations as algebras. At the core of Freestyle algebras is the `@free` macro annotation. `@free` expands abstract traits and classes automatically deriving Algebraic Data types and all the machinery needed to compose them from abstract method definitions.

When you build an algebra with Freestyle, you only need to concentrate on the API that you want to be exposed as abstract smart constructors, without worrying how they will be implemented.

In Freestyle, an **algebra** is a trait or abstract class annotated with `@free` or `@tagless`:


The `Users` trait declares three smart constructors, named `get`, `save`, and `list`, which generate the basic operations in the algebra. A **smart constructor** is an abstract method declaration with a return type of the form `FS[Ret]`, where `Ret` is the type of the data computed by the operation, and `FS[_]`  marks the method as an operation of the algebra. Intuitively, `FS` means that the method gives a computation within some (generic) context or effect `FS`. 
For example, the `save` smart constructor has a return type `FS[User]`. Intuitively, this declares `save` as a computation in a context (or effect) `FS`, whose result will be a `User` object. 

The `@free` is a Scala [macro annotation](https://www.scala-lang.org/blog/2017/10/09/scalamacros.html), that replaces the annotated trait `Users` by a modified trait, and generates a companion object, as in the code below: 
