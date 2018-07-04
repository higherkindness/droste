package qq.droste
package scheme

import cats.Functor
import cats.Monad
import cats.Traverse

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._

import cats.instances.either._
import cats.instances.tuple._

import data._
import syntax._
import implicits.composedFunctor._

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

  /** Builds a hylomorphism the functor `(F ∘ G)`.
    *
    * This is strictly for convenience and just delegates
    * to `hylo` with the types set properly.
    */
  @inline def hyloC[F[_]: Functor, G[_]: Functor, A, B](
    algebra: Algebra[(F ∘ G)#λ, B],
    coalgebra: Coalgebra[(F ∘ G)#λ, A]
  ): A => B = hylo[(F ∘ G)#λ, A, B](algebra, coalgebra)

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
    hyloC[M, F, A, M[B]](
      _.flatMap(_.sequence.flatMap(algebraM)),
      coalgebraM)

  def ana[F[_]: Functor, A, R](
    coalgebra: Coalgebra[F, A]
  )(implicit iso: AlgebraIso[F, R]): A => R =
    hylo(
      iso.algebra,
      coalgebra)

  def cata[F[_]: Functor, R, B](
    algebra: Algebra[F, B]
  )(implicit iso: AlgebraIso[F, R]): R => B =
    hylo(
      algebra,
      iso.coalgebra)


  def anaM[M[_]: Monad, F[_]: Traverse, A, R](
    coalgebraM: CoalgebraM[M, F, A]
  )(implicit iso: AlgebraIso[F, R]): A => M[R] =
    hyloM(
      iso.algebra.lift[M],
      coalgebraM)

  def cataM[M[_]: Monad, F[_]: Traverse, R, B](
    algebraM: AlgebraM[M, F, B]
  )(implicit iso: AlgebraIso[F, R]): R => M[B] =
    hyloM(
      algebraM,
      iso.coalgebra.lift[M])


  /** A variation of an anamorphism that lets you terminate any point of
    * the recursion using a value of the original input type.
    *
    * One use case is to return cached/precomputed results during an
    * unfold.
    */
  def apo[F[_]: Functor, A, R](
    rcoalgebra: RCoalgebra[R, F, A]
  )(implicit iso: AlgebraIso[F, R]): A => R =
    hyloC(
      iso.algebra.compose((frr: F[(R | R)]) => frr.map(_.merge)),
      rcoalgebra)

  /** A variation of a catamorphism that gives you access to the input value at
    * every point in the computation.
    *
    * A paramorphism "eats its argument and keeps it too.
    *
    * This means each step has access to both the computed result
    * value as well as the original value.
    */
  def para[F[_]: Functor, R, B](
    ralgebra: RAlgebra[R, F, B]
  )(implicit iso: AlgebraIso[F, R]): R => B =
    hyloC(
      ralgebra,
      iso.coalgebra.andThen(_.map(r => (r, r))))

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

  implicit def mu[F[_]: Functor]: AlgebraIso[F, Mu[F]] =
    AlgebraIso[F, Mu[F]](Mu.algebra, Mu.coalgebra)

  implicit def nu[F[_]: Functor]: AlgebraIso[F, Nu[F]] =
    AlgebraIso[F, Nu[F]](Nu.algebra, Nu.coalgebra)
}
