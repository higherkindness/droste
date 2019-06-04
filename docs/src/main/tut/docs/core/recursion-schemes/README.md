---
layout: docs
title: Recursion Schemes
permalink: /docs/core/recursion-schemes/
---

# Recursion Schemes

## Basics

### Folds

Folds are used to consume data structures

### Unfolds

Unfolds are used to produce data structures

### Refolds

Refolds go one way and then the other.

## monadic recursion schemes

Monadic recursion schemes are monadic version of already known
recursion schemes.  With monadic version we refer to recursion schemes
in which the return values is wrapped inside a monad:

```scala
def cataM[M[_]: Monad, F[_]: Traverse, R, B](
  algebraM: AlgebraM[M, F, B]
)(implicit project: Project[F, R]): R => M[B]
```

They are marked with an `M` suffix in their name,
and the big difference is that they require a `Traverse` instance in
the pattern functor.  Some examples are `cataM`, `anaM`, or `histoM`.

Monadic recursion schemes are useful when we need to 

## Generalized

## gather / scatter
