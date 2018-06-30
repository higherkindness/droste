package qq.droste
package scheme

import cats.Functor
import cats.Monad
import cats.Traverse

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._

object `package` {

  // note:
  // All core morphisms are defined in an algebra centric manner.
  // All parameters are algebras and a function-- the morphism-- is
  // returned.

  def hylo[F[_]: Functor, A, B](
    algebra: Algebra[F, B],
    coalgebra: Coalgebra[F, A]
  ): A => B =
    new (A => B) {
      def apply(a: A): B = algebra(coalgebra(a).map(this))
    }

  def hyloM[M[_]: Monad, F[_]: Traverse, A, B](
    algebraM: AlgebraM[M, F, B],
    coalgebraM: CoalgebraM[M, F, A]
  ): A => M[B] =
    hylo[(M ∘ F)#λ, A, M[B]](
      _.flatMap(_.sequence.flatMap(algebraM)),
      coalgebraM
    )(Functor[M] compose Functor[F])

}
