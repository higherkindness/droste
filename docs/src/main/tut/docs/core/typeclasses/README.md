---
layout: docs
title: Type Classes
permalink: /docs/core/typeclasses/
---

# Type Classes

There are two main typeclasses in droste, `Project[F[_]]` and
`Embed[F[_]]` respectively.


## Embed

All fixpoint types that allow _lifting_ a pattern functor inside them
have an instance of `Embed`.  `Embed` is declared as follows:

``` scala
trait Embed[F[_], R] {
  def algebra: Algebra[F, R]
}
```

## Project

All fixpoint types from which we can extract a pattern functor have
instances for `Project`.  Project is declared as follows:

``` scala
trait Project[F[_], R] {
  def coalgebra: Coalgebra[F, R]
}
```

One cool feature of `Project` is that it provides `Foldable` operators
if the `F[_]` is a `Foldable` too!

``` tut:silent
import qq.droste.macros.deriveTraverse

@deriveTraverse sealed trait Lambda[A]
object Lambda {
  case class Var[A](name: String) extends Lambda[A]
  case class App[A](f: A, p: A) extends Lambda[A]
  case class Lam[A](name: String, body: A) extends Lambda[A]
}

import qq.droste.syntax.project._

val list: Fix[Lambda] = Fix(App(Fix("a"), Fix(Var("b"))))

val allVars: List[String] = list collect {
  case Var(n) => n
}
```


## Basis

Basis is a typeclass for types that are both an `Embed` and `Project`.
