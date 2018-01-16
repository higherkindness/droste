package droste
package data

import cats.~>
import cats.Id
import alias._

sealed trait FixDecl {
  type Fix[F[_]]
  @inline final def apply[F[_]](f: F[Fix.Fix[F]]): Fix[F] = fix(f)
  @inline def fix[F[_]](f: F[Fix.Fix[F]]): Fix[F]
  @inline def unfix[F[_]](f: Fix[F]): F[Fix.Fix[F]]
}

object `package` {
  type Fix[F[_]] = Fix.Fix[F]
  val Fix: FixDecl = new FixDecl {
    type Fix[F[_]] = F[Fix.Fix[F]]
    def fix[F[_]](f: F[Fix.Fix[F]]): Fix[F] = f
    def unfix[F[_]](f: Fix[F]): F[Fix.Fix[F]] = f
  }

  // Nu/Mu WIP

  // data Mu f = Mu (forall x . (f x -> x) -> x)
  type Mu[F[_]] = Algebra[F, ?] ~> Id

  // data Nu g = forall s . Nu (s -> g s) s
  type Nu[F[_]] = (Coalgebra[F, A], A) forSome { type A }
}
