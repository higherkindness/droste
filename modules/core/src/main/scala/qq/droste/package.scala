package qq.droste

import data.Cofree
import data.Free

object `package` {

  type Algebra    [F[_]      , A] = GAlgebra  [F, A, A]
  type Coalgebra  [F[_]      , A] = GCoalgebra[F, A, A]

  type AlgebraM   [M[_], F[_], A] = GAlgebraM  [M, F, A, A]
  type CoalgebraM [M[_], F[_], A] = GCoalgebraM[M, F, A, A]

  type RAlgebra   [R,    F[_], A] = GAlgebra  [F, (R, A),       A]
  type RCoalgebra [R,    F[_], A] = GCoalgebra[F, A,            Either[R, A]]

  type CVAlgebra  [      F[_], A] = GAlgebra  [F, Cofree[F, A], A]
  type CVCoalgebra[      F[_], A] = GCoalgebra[F, A,            Free[F, A]]

  type Gather     [F[_], S, A] = (A, F[S]) => S
  type Scatter    [F[_], A, S] = S         => Either[A, F[S]]

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
    def apply[M[_], F[_], A](f: A => M[F[A]]): CoalgebraM[M, F, A] = GCoalgebraM(f)
  }

  object RAlgebra {
    def apply[R, F[_], A](f: F[(R, A)] => A): RAlgebra[R, F, A] = GAlgebra(f)
  }

  object RCoalgebra {
    def apply[R, F[_], A](f: A => F[Either[R, A]]): RCoalgebra[R, F, A] = GCoalgebra(f)
  }

  object CVAlgebra {
    def apply[F[_], A](f: F[Cofree[F, A]] => A): CVAlgebra[F, A] = GAlgebra(f)
  }

  object CVCoalgebra {
    def apply[F[_], A](f: A => F[Free[F, A]]): CVCoalgebra[F, A] = GCoalgebra(f)
  }

}
