---
layout: docs
title: annotations
permalink: /docs/core/annotations/
---

# annotations

Droste provides a couple of very useful annotations in the
`droste-macros` module.

## @deriveFixedPoint

`@deriveFixedPoint` can be used in a plain old recursive ADT to
generate the non-recursive counterpart.

There are some restrictions in order to use `@deriveFixedPoint`:
- the annotee should be either a `sealed trait` or `sealed abstract class`
- cases of the ADT should be declared inside the companion object
- recursion should appear on positive postion, somethink like follows will not compile:

```scala mdoc:fail
import higherkindness.droste.macros.deriveFixedPoint

@deriveFixedPoint sealed trait X
object X{
  // here, X appears in negative postition
  case class Y[A](f: X => Int) extends X
}
```


This annotation will create a `object fixedpoint` within the companion
object of the annotated `sealed trait` or `sealed abstract class` with
the following:

- Generate the non-recursive ADT
- create a `Traverse` instance for it
- create a Recursive => NonRecursive[A] coalgebra
- create a NonRecursive[A] => Recursive algebra
- create a `Basis` instance

```scala mdoc
import cats.instances.list._

import higherkindness.droste.Basis
import higherkindness.droste.syntax.all._
import higherkindness.droste.macros.deriveFixedPoint

@deriveFixedPoint sealed trait Expr
object Expr {
  case class Val(i: Int) extends Expr
  case class Sum(a: Expr, b: Expr) extends Expr

  def `val`[A](i: Int): fixedpoint.ExprF[A]  = fixedpoint.ValF(i)
  def sum[A](a: A, b: A): fixedpoint.ExprF[A] = fixedpoint.SumF(a, b)
}

import Expr._
import Expr.fixedpoint._

def program[T: Basis[ExprF, ?]]: T =
  sum[T](
    sum[T](
	  `val`[T](1).embed,
	  `val`[T](2).embed
	).embed,
    sum[T](
	  `val`[T](3).embed,
	  `val`[T](4).embed
	).embed
  ).embed

val numbersToBeAdded = program[Expr].collect[List[Int], Int] {
  case Val(x) => x
}

println(numbersToBeAdded)
```
