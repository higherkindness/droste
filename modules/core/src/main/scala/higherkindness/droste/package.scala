package higherkindness.droste

import cats.~>
import cats.Eq

import data.Attr
import data.Coattr
import syntax.compose._

object `package` {

  /** An `Algebra[F[_], A]` is an alias for an `F[A] => A` function */
  type Algebra[F[_], A] = GAlgebra[F, A, A]

  /** Type Alias `Coalgebra[F, A]` is a type alias for `GCoalgebra[F, A, A]`.
    * This in turn reduces to a `A => F[A]` */
  type Coalgebra[F[_], A] = GCoalgebra[F, A, A]

  /** Type Alias `AlgebraM[M, F, A] = GAlgebraM[M, F, A, A]`, i.e. a `GAlgebraM` where the
    * inner input and output types are the same.
    *
    * This in turn reduces to `M[A] => F[A]`, which looks like `FunctionK[M, F]`,
    * except that the type `A` is not universally quantified, but fixed.
    */
  type AlgebraM[M[_], F[_], A]   = GAlgebraM[M, F, A, A]

  /** Type alias `CoalgebraM[M, F, A] = GCoalgebraM[M, F, A, A]`,
    * a `GCoalgebraM`` whose inner input and output types are the same.
    * This in turn reduces to `A => M[F[A]]`
    */
  type CoalgebraM[M[_], F[_], A] = GCoalgebraM[M, F, A, A]

  /* Reduces to `F[(R, A)] => A` */
  type RAlgebra[R, F[_], A]   = GAlgebra[F, (R, A), A]
  /* Reduces to `A => F[R \/ A]` */
  type RCoalgebra[R, F[_], A] = GCoalgebra[F, A, Either[R, A]]

  type RAlgebraM[R, M[_], F[_], A]   = GAlgebraM[M, F, (R, A), A]
  type RCoalgebraM[R, M[_], F[_], A] = GCoalgebraM[M, F, A, Either[R, A]]

  type CVAlgebra[F[_], A]   = GAlgebra[F, Attr[F, A], A]
  type CVCoalgebra[F[_], A] = GCoalgebra[F, A, Coattr[F, A]]

  /** A `Gather[F, S, A]` is a type alias for `(A, F[S]) => S`, that is a function
    * that starts from an auxiliary value `A` and a collection `F[S]`,
    * and obtains an end result `S`
    */
  type Gather[F[_], S, A]  = (A, F[S]) => S

  /** `Scatter[F[_], A, S]` is an alias for a function `S => Either[A, F[S]]`,
    * that is a function that either arrives to a left result type `A`, or
    * builds a collection `F` of new inputs `S`
    */
  type Scatter[F[_], A, S] = S => Either[A, F[S]]

  object Algebra {
    def apply[F[_], A](f: F[A] => A): Algebra[F, A] = GAlgebra(f)
  }

  object Coalgebra {
    def apply[F[_], A](f: A => F[A]): Coalgebra[F, A] = GCoalgebra(f)
  }

  object AlgebraM {
    def apply[M[_], F[_], A](f: F[A] => M[A]): AlgebraM[M, F, A] = GAlgebraM(f)
  }

  object CoalgebraM {
    def apply[M[_], F[_], A](f: A => M[F[A]]): CoalgebraM[M, F, A] =
      GCoalgebraM(f)
  }

  object RAlgebra {
    def apply[R, F[_], A](f: F[(R, A)] => A): RAlgebra[R, F, A] = GAlgebra(f)
  }

  object RCoalgebra {
    def apply[R, F[_], A](f: A => F[Either[R, A]]): RCoalgebra[R, F, A] =
      GCoalgebra(f)
  }

  object RAlgebraM {
    def apply[R, M[_], F[_], A](f: F[(R, A)] => M[A]): RAlgebraM[R, M, F, A] =
      GAlgebraM(f)
  }

  object RCoalgebraM {
    def apply[R, M[_], F[_], A](
        f: A => M[F[Either[R, A]]]): RCoalgebraM[R, M, F, A] =
      GCoalgebraM(f)
  }

  object CVAlgebra {
    def apply[F[_], A](f: F[Attr[F, A]] => A): CVAlgebra[F, A] = GAlgebra(f)
  }

  object CVCoalgebra {
    def apply[F[_], A](f: A => F[Coattr[F, A]]): CVCoalgebra[F, A] =
      GCoalgebra(f)
  }

  type Trans[F[_], G[_], A]        = GTrans[F, G, A, A]
  type TransM[M[_], F[_], G[_], A] = GTransM[M, F, G, A, A]

  object Trans {
    def apply[F[_], G[_], A](f: F[A] => G[A]): Trans[F, G, A] = GTrans(f)
  }

  object TransM {
    def apply[M[_], F[_], G[_], A](f: F[A] => M[G[A]]): TransM[M, F, G, A] =
      GTransM(f)
  }

  type Delay[F[_], G[_]] = F ~> (F ∘ G)#λ
}

object prelude {
  implicit def drosteDelayedEq[Z, F[_]](
      implicit p: Project[F, Z],
      delay: Delay[Eq, F]): Eq[Z] = {
    lazy val knot: Eq[Z] =
      Eq.instance((x, y) => delay(knot).eqv(p.coalgebra(x), p.coalgebra(y)))
    knot
  }

  // todo: where should this live?
  implicit val drosteDelayEqOption: Delay[Eq, Option] =
    λ[Eq ~> (Eq ∘ Option)#λ](eq =>
      Eq.instance((x, y) =>
        x.fold(y.fold(true)(_ => false))(xx =>
          y.fold(false)(yy => eq.eqv(xx, yy)))))
}
