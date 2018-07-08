package qq.droste

import cats.Functor
import cats.Monad
import cats.Traverse

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._

import cats.instances.either._
import cats.instances.tuple._

import syntax._
import implicits.composedFunctor._

object scheme {

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
  )(implicit embed: Embed[F, R]): A => R =
    hylo(
      embed.algebra,
      coalgebra)

  def cata[F[_]: Functor, R, B](
    algebra: Algebra[F, B]
  )(implicit project: Project[F, R]): R => B =
    hylo(
      algebra,
      project.coalgebra)


  def anaM[M[_]: Monad, F[_]: Traverse, A, R](
    coalgebraM: CoalgebraM[M, F, A]
  )(implicit embed: Embed[F, R]): A => M[R] =
    hyloM(
      embed.algebra.lift[M],
      coalgebraM)

  def cataM[M[_]: Monad, F[_]: Traverse, R, B](
    algebraM: AlgebraM[M, F, B]
  )(implicit project: Project[F, R]): R => M[B] =
    hyloM(
      algebraM,
      project.coalgebra.lift[M])


  /** A variation of an anamorphism that lets you terminate any point of
    * the recursion using a value of the original input type.
    *
    * One use case is to return cached/precomputed results during an
    * unfold.
    */
  def apo[F[_]: Functor, A, R](
    rcoalgebra: RCoalgebra[R, F, A]
  )(implicit embed: Embed[F, R]): A => R =
    hyloC(
      embed.algebra.compose((frr: F[(R | R)]) => frr.map(_.merge)),
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
  )(implicit project: Project[F, R]): R => B =
    hyloC(
      ralgebra,
      project.coalgebra.andThen(_.map(r => (r, r))))


  /** Convenience to specify the base constructor (such as `Fix` or
    * `Cofree[?[_], Int]`) for recursion.
    *
    * This helps to guide Scala's type inference so all of the type
    * parameters for the various recursion scheme methods don't have
    * to be provided.
    */
  def apply[H[_[_]]](): SchemePartialBasis[H] = new SchemePartialBasis[H]

  final class SchemePartialBasis[H[_[_]]] private[droste]() {

    def ana[F[_]: Functor, A](
      coalgebra: Coalgebra[F, A]
    )(implicit ev: Embed[F, H[F]]): A => H[F] =
      scheme.ana[F, A, H[F]](coalgebra)

    def anaM[M[_]: Monad, F[_]: Traverse, A](
      coalgebraM: CoalgebraM[M, F, A]
    )(implicit embed: Embed[F, H[F]]): A => M[H[F]] =
      scheme.anaM[M, F, A, H[F]](coalgebraM)

    def apo[F[_]: Functor, A](
      rcoalgebra: RCoalgebra[H[F], F, A]
    )(implicit embed: Embed[F, H[F]]): A => H[F] =
      scheme.apo[F, A, H[F]](rcoalgebra)

  }

}
