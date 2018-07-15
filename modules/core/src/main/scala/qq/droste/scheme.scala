package qq.droste

import cats.Functor
import cats.Monad
import cats.Traverse

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._

import cats.instances.either._
import cats.instances.tuple._

import data.prelude._
import data.Cofree
import data.Free
import syntax.all._
import implicits.composedFunctor._

/**
  * @groupname refolds Refolds
  * @groupname folds   Folds
  * @groupname unfolds Unfolds
  */
object scheme {

  // note:
  // All core morphisms are defined in an algebra centric manner.
  // All parameters are algebras and a function-- the morphism-- is
  // returned.

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
    * @usecase def hylo[F[_], A, B](algebra: Algebra[F, B], coalgebra: Coalgebra[F, A]): A => B
    *   @inheritdoc
    */
  def hylo[F[_]: Functor, A, B](
    algebra: Algebra[F, B],
    coalgebra: Coalgebra[F, A]
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
    * @usecase def hyloC[F[_], G[_], A, B](algebra: Algebra[(F ∘ G)#λ, B], coalgebra: Coalgebra[(F ∘ G)#λ, A]): A => B
    *   @inheritdoc
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
    *
    * @group refolds
    *
    * @usecase def hyloM[M[_], F[_], A, B](algebra: AlgebraM[M, F, B], coalgebra: CoalgebraM[M, F, A]): A => M[B]
    *   @inheritdoc
    */
  def hyloM[M[_]: Monad, F[_]: Traverse, A, B](
    algebra: AlgebraM[M, F, B],
    coalgebra: CoalgebraM[M, F, A]
  ): A => M[B] =
    hyloC[M, F, A, M[B]](
      _.flatMap(_.sequence.flatMap(algebra)),
      coalgebra)

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
    *
    * @usecase def apo[F[_], A, R](coalgebra: RCoalgebra[R, F, A]): A => R
    *   @inheritdoc
    */
  def apo[F[_]: Functor, A, R](
    coalgebra: RCoalgebra[R, F, A]
  )(implicit embed: Embed[F, R]): A => R =
    hyloC(
      embed.algebra.compose((frr: F[(R | R)]) => frr.map(_.merge)),
      coalgebra)

  /** A variation of a catamorphism that gives you access to the input value at
    * every point in the computation.
    *
    * A paramorphism "eats its argument and keeps it too.
    *
    * This means each step has access to both the computed result
    * value as well as the original value.
    *
    * @usecase def para[F[_], R, B](algebra: RAlgebra[R, F, B]): R => B
    *   @inheritdoc
    */
  def para[F[_]: Functor, R, B](
    algebra: RAlgebra[R, F, B]
  )(implicit project: Project[F, R]): R => B =
    hyloC(
      algebra,
      project.coalgebra.andThen(_.map(r => (r, r))))


  /** Histomorphism
    *
    * @usecase def histo[F[_], R, B](algebra: CVAlgebra[F, B]): R => B
    *   @inheritdoc
    */
  def histo[F[_]: Functor, R, B](
    algebra: CVAlgebra[F, B]
  )(implicit project: Project[F, R]): R => B =
    hylo[F, R, Cofree[F, B]](
      fb => Cofree(algebra(fb), fb),
      project.coalgebra
    ) andThen (_.head)

  /** Futumorphism
    *
    * @usecase def futu[F[_], A, R](coalgebra: CVCoalgebra[F, A]): A => R
    *   @inheritdoc
    */
  def futu[F[_]: Functor, A, R](
    coalgebra: CVCoalgebra[F, A]
  )(implicit embed: Embed[F, R]): A => R =
    hylo[F, Free[F, A], R](
      embed.algebra,
      _.fold(coalgebra, identity)
    ) compose (Free.pure(_))

  /** A fusion refold of a futumorphism followed by a histomorphism
    *
    * @group refolds
    *
    * @usecase def chrono[F[_], A, B](algebra: CVAlgebra[F, B], coalgebra: CVCoalgebra[F, A]): A => B
    *   @inheritdoc
    */
  def chrono[F[_]: Functor, A, B](
    algebra: CVAlgebra[F, B],
    coalgebra: CVCoalgebra[F, A]
  ): A => B =
    hylo[F, Free[F, A], Cofree[F, B]](
      fb => Cofree(algebra(fb), fb),
      _.fold(coalgebra, identity)
    ) andThen (_.head) compose (Free.pure(_))

  /** A fusion refold of an anamorphism followed by a histomorphism
    *
    * @group refolds
    *
    * @usecase def dyna[F[_], A, B](algebra: CVAlgebra[F, B], coalgebra: Coalgebra[F, A]): A => B
    *   @inheritdoc
    */
  def dyna[F[_]: Functor, A, B](
    algebra: CVAlgebra[F, B],
    coalgebra: Coalgebra[F, A]
  ): A => B =
    hylo[F, A, Cofree[F, B]](
      fb => Cofree(algebra(fb), fb),
      coalgebra
    ) andThen (_.head)

  /** Convenience to specify the base constructor "shape" (such as `Fix`
    * or `Cofree[?[_], Int]`) for recursion.
    *
    * This helps to guide Scala's type inference so all of the type
    * parameters for the various recursion scheme methods don't have
    * to be provided.
    *
    * @usecase def apply[Shape]: SchemePartialBasis[Shape, Shape]
    *   @inheritdoc
    */
  def apply[PatR[_[_]]](implicit ev: Basis.Solve[PatR]): SchemePartialBasis[PatR, ev.PatF] = new SchemePartialBasis[PatR, ev.PatF]

  final class SchemePartialBasis[PatR[_[_]], PatF[_[_], _]] private[droste]() {

    type EmbedP[F[_]]    = Embed[PatF[F, ?], PatR[F]]
    type ProjectP[F[_]]  = Project[PatF[F, ?], PatR[F]]
    type FunctorP[F[_]]  = Functor[PatF[F, ?]]
    type TraverseP[F[_]] = Traverse[PatF[F, ?]]

    def ana[F[_], A](
      coalgebra: Coalgebra[PatF[F, ?], A]
    )(implicit embed: EmbedP[F], ev: FunctorP[F]): A => PatR[F] =
      scheme.ana[PatF[F, ?], A, PatR[F]](coalgebra)

    def anaM[M[_]: Monad, F[_], A](
      coalgebraM: CoalgebraM[M, PatF[F, ?], A]
    )(implicit embed: EmbedP[F], ev: TraverseP[F]): A => M[PatR[F]] =
      scheme.anaM[M, PatF[F, ?], A, PatR[F]](coalgebraM)

    def apo[F[_], A](
      rcoalgebra: RCoalgebra[PatR[F], PatF[F, ?], A]
    )(implicit embed: EmbedP[F], ev: FunctorP[F]): A => PatR[F] =
      scheme.apo[PatF[F, ?], A, PatR[F]](rcoalgebra)

    def futu[F[_], A](
      cvcoalgebra: CVCoalgebra[PatF[F, ?], A]
    )(implicit embed: EmbedP[F], ev: FunctorP[F]): A => PatR[F] =
      scheme.futu[PatF[F, ?], A, PatR[F]](cvcoalgebra)


    def cata[F[_], B](
      algebra: Algebra[PatF[F, ?], B]
    )(implicit project: ProjectP[F], ev: FunctorP[F]): PatR[F] => B =
      scheme.cata[PatF[F, ?], PatR[F], B](algebra)

  }

}
