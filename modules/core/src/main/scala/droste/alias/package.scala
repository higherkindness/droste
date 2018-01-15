package droste
package alias

import cats.~>

object `package` {
  type Algebra   [F[_]      , A   ] = F[A]    =>   A
  type Coalgebra [F[_]      , A   ] = A       => F[A]
  type AlgebraK  [F[_[_], _], A[_]] = F[A, ?] ~>   A
  type CoalgebraK[F[_[_], _], A[_]] = A       ~> F[A, ?]

  type AlgebraM  [M[_], F[_], A   ] = F[A]    =>   M[A]
  type CoalgebraM[M[_], F[_], A   ] = A       => M[F[A]]

  type ∘[F[_], G[_]] = { type λ[α] = F[G[α]] }
}
