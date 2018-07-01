package qq.droste
package scheme

import cats.Functor
import cats.Monad
import cats.Traverse

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._

import data._
import syntax._

object `package` {

  // note:
  // All core morphisms are defined in an algebra centric manner.
  // All parameters are algebras and a function-- the morphism-- is
  // returned.

  /** Build a hylomorphism (a function `A => B`) by recursively
    * unfolding with `coalgebra` and refolding with `algebra`.
    *
    * <pre>
    *                  hylo
    *          A ---------------> B
    *          |                  ^
    *  co-     |                  |
    * algebra  |                  | algebra
    *          |                  |
    *          v                  |
    *         F[A] ------------> F[B]
    *                map hylo
    * </pre>
    */
  def hylo[F[_]: Functor, A, B](
    algebra: Algebra[F, B],
    coalgebra: Coalgebra[F, A]
  ): A => B =
    new (A => B) {
      def apply(a: A): B = algebra(coalgebra(a).map(this))
    }

  /** Build a monadic hylomorphism
    *
    * <pre>
    *                 hyloM
    *          A ---------------> M[B]
    *          |                  ^
    *  co-     |                  |
    * algebraM |                  | flatMap f
    *          |                  |
    *          v                  |
    *       M[F[A]] ---------> M[F[M[B]]]
    *               map hyloM
    *
    * with f:
    *
    * F[M[B]] -----> M[F[B]] ----------> M[B]
    *       sequence          flatMap
    *                         algebraM
    * </pre>
    */
  def hyloM[M[_]: Monad, F[_]: Traverse, A, B](
    algebraM: AlgebraM[M, F, B],
    coalgebraM: CoalgebraM[M, F, A]
  ): A => M[B] =
    hylo[(M ∘ F)#λ, A, M[B]](
      _.flatMap(_.sequence.flatMap(algebraM)),
      coalgebraM
    )(Functor[M] compose Functor[F])

  def ana[F[_]: Functor, A, R](
    coalgebra: Coalgebra[F, A],
  )(implicit iso: AlgebraIso[F, R]): A => R =
    hylo(iso.algebra, coalgebra)

  def anaM[M[_]: Monad, F[_]: Traverse, A, R](
    coalgebraM: CoalgebraM[M, F, A],
  )(implicit iso: AlgebraIso[F, R]): A => M[R] =
    hyloM(iso.algebra.lift[M], coalgebraM)

  def cata[F[_]: Functor, B, R](
    algebra: Algebra[F, B],
  )(implicit iso: AlgebraIso[F, R]): R => B =
    hylo(algebra, iso.coalgebra)

  def cataM[M[_]: Monad, F[_]: Traverse, R, B](
    algebraM: AlgebraM[M, F, B]
  )(implicit iso: AlgebraIso[F, R]): R => M[B] =
    hyloM(algebraM, iso.coalgebra.lift[M])
}

final case class AlgebraIso[F[_], R](
  algebra: Algebra[F, R],
  coalgebra: Coalgebra[F, R])

object AlgebraIso extends AlgebraIsoInstances0

private[droste] sealed trait AlgebraIsoInstances0 extends AlgebraIsoInstances1 {
  implicit def cofree[F[_], E]: AlgebraIso[EnvT[E, F, ?], Cofree[F, E]] =
    AlgebraIso[EnvT[E, F, ?], Cofree[F, E]](Cofree.algebra, Cofree.coalgebra)
}

private[droste] sealed trait AlgebraIsoInstances1 {
  implicit def fix[F[_]]: AlgebraIso[F, Fix[F]] =
    AlgebraIso[F, Fix[F]](Fix.algebra, Fix.coalgebra)
}
