---
layout: docs
title: fixpoint types
permalink: /docs/core/fixpoint-types/
---

# Fixpoint types

The basic idea of recursion schemes is that we factor the recursion
out of recursive data types.  We do so by converting recursive data
types:

``` scala mdoc
sealed trait Expr
case class Sum(a: Expr, b: Expr) extends Expr
case class Val(a: Int) extends Expr
```

into non-recursive data types, in which the recursion is factored to a
type parameter.

``` scala mdoc
sealed trait ExprF[A]
case class SumF[A](a: A, b: A) extends ExprF[A]
case class ValF[A](a: Int) extends ExprF[A]
```

As you can see, we replace occurences of explicit recursion with the
type parameter `A`.

However, how do we create values using our new ADT?  Imagine we want
to represent the `1 + 2` expression.  In the first version of
`Expr`, it's easy:

``` scala mdoc
val sum: Expr = Sum(Val(1), Val(2))
```

However, using the second type is not as easy, if we try to follow the
same approach, we see that something doesn't work:

``` scala mdoc
val sumF: ExprF[ExprF[Int]] = SumF(ValF(1), ValF(2))
```

What's the type of `sumF` now? If we'd start substituting we'd get
something like `ExprF[ExprF[...]]`, and that's not what we want.

Introducing fixpoint types.

Fixpoint types are the missing piece in the previous approach to
datatypes, they tie the recursive knot in order to avoid scenarios
such as `ExprF[ExprF[ExprF[...]]]`.

## Fix

Fix is the simplest of fixpoint data types, and it's declaration is as
follows:

``` scala
case class Fix[F[_]](unFix: F[Fix[F]])
```

Even though it may look extrange, the idea is quite simple.  Let's get
back to the previous example.  Using `Fix` we can now define our value
sumF as follows:

``` scala
val sumF: Fix[ExprF] = Fix(SumF(Fix(ValF(1)), Fix(ValF(2))))
```

## Mu

`Mu` is a fixpoint datatype that is declared as the fold (`cata`) of a
datastructure.

## Nu

`Nu` is a fixpoint type declared as the unfold (`ana`) of a datastructure.

## Coattr

`Coattr`, also known as `Free`, is a fixpoint type in which the leaves
of the tree are annotated with an additional value.

## Attr

`Attr`, also known as `Cofree`, is a fixpoint type in which al levels
of the tree are annotated with an additional value.  Using `Attr` is
very useful when you need to add an annotation, or attribute, to the
values in your pattern functor.

`Attr` can be used for:
* [annotating an AST with types](https://brianmckenna.org/blog/type_annotation_cofree)
* [annotating an AST with positions in the source file](https://github.com/haskell-nix/hnix/blob/master/src/Nix/Expr/Types/Annotated.hs).

# Pattern Functors

Pattern functors are the 
