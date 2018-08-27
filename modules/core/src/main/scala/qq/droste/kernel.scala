package qq.droste

import cats.Functor
import cats.Monad
import cats.Traverse

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._

import implicits.composedFunctor._
import implicits.composedTraverse._
import syntax.compose._

/** Fundamental recursion schemes implemented in terms of
  * functions and nothing else.
  *
  */
object kernel {

  /** Build a hylomorphism by recursively unfolding with `coalgebra` and
    * refolding with `algebra`.
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
    *
    * @group refolds
    *
    * @usecase def hylo[F[_], A, B](algebra: F[B] => B, coalgebra: A => F[A]): A => B
    *   @inheritdoc
    */
  def hylo[F[_]: Functor, A, B](
    algebra  : F[B] => B,
    coalgebra: A    => F[A]
  ): A => B =
    new (A => B) {
      def apply(a: A): B = algebra(coalgebra(a).map(this))
    }

  /** Convenience to build a hylomorphism for the composed functor `F[G[_]]`.
    *
    * This is strictly for convenience and just delegates
    * to `hylo` with the types set properly.
    *
    * @group refolds
    *
    * @usecase def hyloC[F[_], G[_], A, B](algebra: F[G[B]] => B, coalgebra: A => F[G[A]]): A => B
    *   @inheritdoc
    */
  @inline def hyloC[F[_]: Functor, G[_]: Functor, A, B](
    algebra  : F[G[B]] => B,
    coalgebra: A       => F[G[A]]
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
    *
    * @group refolds
    *
    * @usecase def hyloM[M[_], F[_], A, B](algebra: F[B] => M[B], coalgebra: A => M[F[A]]): A => M[B]
    *   @inheritdoc
    */
  def hyloM[M[_]: Monad, F[_]: Traverse, A, B](
    algebra  : F[B] => M[B],
    coalgebra: A => M[F[A]]
  ): A => M[B] =
    hyloC[M, F, A, M[B]](
      _.flatMap(_.sequence.flatMap(algebra)),
      coalgebra)

  /** Convenience to build a monadic hylomorphism for the composed functor `F[G[_]]`.
    *
    * @group refolds
    *
    * @usecase def hyloMC[M[_], F[_], G[_], A, B](algebra: F[G[B]] => M[B], coalgebra: A => M[F[G[A]]]): A => M[B]
    *   @inheritdoc
    */
  def hyloMC[M[_]: Monad, F[_]: Traverse, G[_]: Traverse, A, B](
    algebra  : F[G[B]] => M[B],
    coalgebra: A => M[F[G[A]]]
  ): A => M[B] =
    hyloM[M, (F ∘ G)#λ, A, B](algebra, coalgebra)

}
