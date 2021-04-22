---
layout: docs
title: Type Classes
permalink: /docs/core/typeclasses/
---

# Type Classes

There are two main typeclasses in droste, `Project[F[_]]` and
`Embed[F[_]]` .


## Embed

All [fixpoint types](/docs/core/fixpoint-types/) that allow _lifting_ a pattern functor inside them
have an instance of `Embed`.  `Embed` is declared as follows:

```scala
trait Embed[F[_], R] {
  def algebra: Algebra[F, R]
}
```

## Project

All [fixpoint types](/docs/core/fixpoint-types) from which we can extract a pattern functor have
instances for `Project`.  Project is declared as follows:

```scala
trait Project[F[_], R] {
  def coalgebra: Coalgebra[F, R]
}
```

## Basis

Basis is a typeclass for types that are both an `Embed` and `Project`.
