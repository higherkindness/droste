package qq.droste

import data.Cofree
import data.Free
import syntax.alias._

object `package` {

  type Algebra    [F[_]      , A   ] = F[A]            => A
  type Coalgebra  [F[_]      , A   ] = A               => F[A]

  type AlgebraM   [M[_], F[_], A   ] = F[A]            => M[A]
  type CoalgebraM [M[_], F[_], A   ] = A               => M[F[A]]

  type RAlgebra   [R,    F[_], A   ] = F[R & A]        => A
  type RCoalgebra [R,    F[_], A   ] = A               => F[R | A]

  type CVAlgebra  [      F[_], A   ] = F[Cofree[F, A]] => A
  type CVCoalgebra[      F[_], A   ] = A               => F[Free[F, A]]

  type GAlgebra   [W[_], F[_], A   ] = F[W[A]]         => A
  type GCoalgebra [W[_], F[_], A   ] = A               => F[W[A]]

}
