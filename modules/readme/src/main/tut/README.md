<img align="right" src="logos/droste_cocoa.png" height="200px" style="padding-left: 20px"/>

# Droste

[![Join the chat at https://gitter.im/higherkindness/droste](https://badges.gitter.im/higherkindness/droste.svg)](https://gitter.im/higherkindness/droste?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Droste is a recursion library for Scala.

**SBT installation**

Select a tagged release version (`x.y.z`) and then add the following
to your SBT build:

```scala
libraryDependencies += "io.higherkindness" %% "droste-core" % "x.y.z"
```

# Usage

Droste makes it easy to assemble morphisms. For example, calculating
Fibonacci values can be done with a histomorphism if we model natural
numbers as a chain of `Option`. We can easily unfold with an
anamorphism and then fold to our result with a histomorphism.

```tut:silent
import higherkindness.droste._
import higherkindness.droste.data._
import cats.implicits._

val natCoalgebra: Coalgebra[Option, BigDecimal] =
  Coalgebra(n => if (n > 0) Some(n - 1) else None)

val fibAlgebra: CVAlgebra[Option, BigDecimal] = CVAlgebra {
  case Some(r1 :< Some(r2 :< _)) => r1 + r2
  case Some(_ :< None)           => 1
  case None                      => 0
}

val fib: BigDecimal => BigDecimal = scheme.ghylo(
  fibAlgebra.gather(Gather.histo),
  natCoalgebra.scatter(Scatter.ana))
```

```tut
fib(0)
fib(1)
fib(2)
fib(10)
fib(100)
```

An anamorphism followed by a histomorphism is also known as a
dynamorphism. Recursion scheme animals like dyna are available
in the zoo:

```tut:silent

val fibAlt: BigDecimal => BigDecimal =
  scheme.zoo.dyna(fibAlgebra, natCoalgebra)
```

```tut
fibAlt(0)
fibAlt(1)
fibAlt(2)
fibAlt(10)
fibAlt(100)
```

What if we want to do two things at once? Let's calculate a
Fibonacci value and the sum of all squares.

```tut:silent
val fromNatAlgebra: Algebra[Option, BigDecimal] = Algebra {
  case Some(n) => n + 1
  case None    => 0
}

// note: n is the fromNatAlgebra helper value from the previous level of recursion
val sumSquaresAlgebra: RAlgebra[BigDecimal, Option, BigDecimal] = RAlgebra {
  case Some((n, value)) => value + (n + 1) * (n + 1)
  case None             => 0
}

val sumSquares: BigDecimal => BigDecimal = scheme.ghylo(
  sumSquaresAlgebra.gather(Gather.zygo(fromNatAlgebra)),
  natCoalgebra.scatter(Scatter.ana))
```

```tut
sumSquares(0)
sumSquares(1)
sumSquares(2)
sumSquares(10)
sumSquares(100)
```

Now we can zip the two algebras into one so that we calculate
both results in one pass.

```tut:silent
val fused: BigDecimal => (BigDecimal, BigDecimal) =
  scheme.ghylo(
    fibAlgebra.gather(Gather.histo) zip
    sumSquaresAlgebra.gather(Gather.zygo(fromNatAlgebra)),
    natCoalgebra.scatter(Scatter.ana))
```

```tut
fused(0)
fused(1)
fused(2)
fused(10)
fused(100)
```

Droste includes [athema](athema), a math expression parser/processor,
as a more extensive example of recursion schemes.

<img align="right" src="logos/droste_psychedelic_1.png" height="85px" style="padding-left: 5px"/>

# Credits

A substantial amount of Droste's code is a derivation-- or an
alternative encoding-- of patterns pioneered by others. Droste has
benefited from the excellent work in many other recursion libraries,
blog posts, academic papers, etc. Notably, Droste has benefited from:

- [recursion-schemes](https://github.com/ekmett/recursion-schemes)
- [Matryoshka](https://github.com/slamdata/matryoshka)

Thank you to everyone involved. Additionally, thanks to Greg Pfeil
(@sellout) for answering my random questions over the last few years
while I've been slowly learning (and using recursion) schemes.

<img align="right" src="logos/droste_psychedelic_2.png" height="85px" style="padding-left: 5px"/>

# Copyright and License

Copyright the maintainers, 2018-present.

All code is available to you under the Apache License, Version 2.0,
available at http://www.apache.org/licenses/LICENSE-2.0.

Logos provided by the very excellent [@impurepics](https://twitter.com/impurepics).

# Disclamer

Please be advised that I have no idea what I am doing.
Nevertheless, this project is already being used for real
work with real data in real life.
