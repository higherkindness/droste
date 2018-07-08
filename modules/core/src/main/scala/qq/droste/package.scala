package qq.droste

import syntax._

object `package` {

  type Algebra   [F[_]      , A   ] = F[A]    =>   A
  type Coalgebra [F[_]      , A   ] = A       => F[A]

  type AlgebraM  [M[_], F[_], A   ] = F[A]    =>   M[A]
  type CoalgebraM[M[_], F[_], A   ] = A       => M[F[A]]

  type RAlgebra  [R, F[_], A   ] = F[R & A] =>   A
  type RCoalgebra[R, F[_], A   ] = A        => F[R | A]

}
