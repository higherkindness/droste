package higherkindness.droste

import cats.Functor
import cats.Monad
import cats.Traverse

import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._

import implicits.composedFunctor._

/**
  * @groupname refolds Refolds
  * @groupname folds   Folds
  * @groupname unfolds Unfolds
  * @groupname exotic  Exotic
  */
object scheme
    extends SchemeHyloPorcelain
    with SchemeConvenientPorcelain
    with SchemeGeneralizedPorcelain {

  /** A petting zoo for wild and exotic animals we keep separate from
    * the regulars in [[scheme]]. For their safety and yours.
    *
    * @group exotic
    *
    * @groupname refolds Rambunctious Refolds
    * @groupname folds   Fantastic Folds
    * @groupname unfolds Unusual Unfolds
    */
  object zoo extends Zoo
}

private[droste] sealed trait SchemeConvenientPorcelain {

  // these _could_ go in the zoo, but they are very common for people
  // learning recursion schemes, so it's nice to have them here

  def ana[F[_]: Functor, A, R](
      coalgebra: Coalgebra[F, A]
  )(implicit embed: Embed[F, R]): A => R =
    kernel.hylo(embed.algebra.apply, coalgebra.apply)

  def anaM[M[_]: Monad, F[_]: Traverse, A, R](
      coalgebraM: CoalgebraM[M, F, A]
  )(implicit embed: Embed[F, R]): A => M[R] =
    kernel.hyloM(embed.algebra.lift[M].apply, coalgebraM.apply)

  def cata[F[_]: Functor, R, B](
      algebra: Algebra[F, B]
  )(implicit project: Project[F, R]): R => B =
    kernel.hylo(algebra.apply, project.coalgebra.apply)

  def cataM[M[_]: Monad, F[_]: Traverse, R, B](
      algebraM: AlgebraM[M, F, B]
  )(implicit project: Project[F, R]): R => M[B] =
    kernel.hyloM(algebraM.apply, project.coalgebra.lift[M].apply)

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
  def apply[PatR[_[_]]](
      implicit ev: Basis.Solve[PatR]): SchemePartialBasis[PatR, ev.PatF] =
    new SchemePartialBasis[PatR, ev.PatF]

  final class SchemePartialBasis[PatR[_[_]], PatF[_[_], _]] private[droste] () {

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

    def gana[F[_], A, S](
        scattered: GCoalgebra.Scattered[PatF[F, ?], A, S]
    )(implicit embed: EmbedP[F], ev: FunctorP[F]): A => PatR[F] =
      scheme.gana[PatF[F, ?], A, S, PatR[F]](scattered.coalgebra)(
        scattered.scatter)

    def ganaM[M[_]: Monad, F[_], A, S](
        scattered: GCoalgebraM.Scattered[M, PatF[F, ?], A, S]
    )(implicit embed: EmbedP[F], ev: TraverseP[F]): A => M[PatR[F]] =
      scheme.ganaM[M, PatF[F, ?], A, S, PatR[F]](scattered)

    def cata[F[_], B](
        algebra: Algebra[PatF[F, ?], B]
    )(implicit project: ProjectP[F], ev: FunctorP[F]): PatR[F] => B =
      scheme.cata[PatF[F, ?], PatR[F], B](algebra)

    def cataM[M[_]: Monad, F[_], B](
        algebraM: AlgebraM[M, PatF[F, ?], B]
    )(implicit project: ProjectP[F], ev: TraverseP[F]): PatR[F] => M[B] =
      scheme.cataM[M, PatF[F, ?], PatR[F], B](algebraM)

    def gcata[F[_], S, B](
        gathered: GAlgebra.Gathered[PatF[F, ?], S, B]
    )(implicit project: ProjectP[F], ev: FunctorP[F]): PatR[F] => B =
      scheme.gcata[PatF[F, ?], PatR[F], S, B](gathered.algebra)(gathered.gather)

    def gcataM[M[_]: Monad, F[_], S, B](
        gathered: GAlgebraM.Gathered[M, PatF[F, ?], S, B]
    )(implicit project: ProjectP[F], ev: TraverseP[F]): PatR[F] => M[B] =
      scheme.gcataM[M, PatF[F, ?], PatR[F], S, B](gathered)

  }

}

private[droste] sealed trait SchemeGeneralizedPorcelain
    extends SchemeGeneralizedPlumbing {

  def ghylo[F[_]: Functor, A, SA, SB, B](
      gathered: GAlgebra.Gathered[F, SB, B],
      scattered: GCoalgebra.Scattered[F, A, SA]
  ): A => B =
    ghylo(gathered.algebra, scattered.coalgebra)(
      gathered.gather,
      scattered.scatter)

  def ghyloM[M[_]: Monad, F[_]: Traverse, A, SA, SB, B](
      gathered: GAlgebraM.Gathered[M, F, SB, B],
      scattered: GCoalgebraM.Scattered[M, F, A, SA]
  ): A => M[B] =
    ghyloM(gathered.algebra, scattered.coalgebra)(
      gathered.gather,
      scattered.scatter)

  def gcata[F[_]: Functor, R, S, B](
      gathered: GAlgebra.Gathered[F, S, B]
  )(implicit project: Project[F, R]): R => B =
    gcata(gathered.algebra)(gathered.gather)

  def gcataM[M[_]: Monad, F[_]: Traverse, R, S, B](
      gathered: GAlgebraM.Gathered[M, F, S, B]
  )(implicit project: Project[F, R]): R => M[B] =
    gcataM(gathered.algebra)(gathered.gather)

  def gana[F[_]: Functor, A, S, R](
      scattered: GCoalgebra.Scattered[F, A, S]
  )(implicit embed: Embed[F, R]): A => R =
    gana(scattered.coalgebra)(scattered.scatter)

  def ganaM[M[_]: Monad, F[_]: Traverse, A, S, R](
      scattered: GCoalgebraM.Scattered[M, F, A, S]
  )(implicit embed: Embed[F, R]): A => M[R] =
    ganaM(scattered.coalgebra)(scattered.scatter)

}

private[droste] sealed trait SchemeGeneralizedPlumbing {

  def ghylo[F[_]: Functor, A, SA, SB, B](
      algebra: GAlgebra[F, SB, B],
      coalgebra: GCoalgebra[F, A, SA])(
      gather: Gather[F, SB, B],
      scatter: Scatter[F, A, SA]
  ): A => B =
    a =>
      algebra(
        coalgebra(a).map(
          kernel.hylo[F, SA, SB](
            fb => gather(algebra(fb), fb),
            sa => scatter(sa).fold(coalgebra.apply, identity))))

  def ghyloM[M[_]: Monad, F[_]: Traverse, A, SA, SB, B](
      algebra: GAlgebraM[M, F, SB, B],
      coalgebra: GCoalgebraM[M, F, A, SA])(
      gather: Gather[F, SB, B],
      scatter: Scatter[F, A, SA]
  ): A => M[B] =
    a =>
      coalgebra(a).flatMap(
        fsa =>
          fsa
            .traverse(
              kernel.hyloM[M, F, SA, SB](
                fb => algebra(fb).map(gather(_, fb)),
                sa => scatter(sa).fold(coalgebra.apply, _.pure[M])))
            .flatMap(algebra.apply))

  def gcata[F[_]: Functor, R, S, B](galgebra: GAlgebra[F, S, B])(
      gather: Gather[F, S, B]
  )(implicit project: Project[F, R]): R => B =
    r =>
      galgebra(
        project
          .coalgebra(r)
          .map(
            kernel.hylo[F, R, S](
              fb => gather(galgebra(fb), fb),
              project.coalgebra.apply)))

  def gcataM[M[_]: Monad, F[_]: Traverse, R, S, B](
      algebra: GAlgebraM[M, F, S, B])(
      gather: Gather[F, S, B]
  )(implicit project: Project[F, R]): R => M[B] =
    r =>
      project
        .coalgebra(r)
        .traverse(
          kernel
            .hyloM[M, F, R, S](
              fb => algebra(fb).map(gather(_, fb)),
              project.coalgebra.lift[M].apply))
        .flatMap(algebra.apply)

  def gana[F[_]: Functor, A, S, R](coalgebra: GCoalgebra[F, A, S])(
      scatter: Scatter[F, A, S]
  )(implicit embed: Embed[F, R]): A => R =
    a =>
      embed.algebra(
        coalgebra(a).map(
          kernel.hylo[F, S, R](
            embed.algebra.apply,
            s => scatter(s).fold(coalgebra.apply, identity))))

  def ganaM[M[_]: Monad, F[_]: Traverse, A, S, R](
      coalgebra: GCoalgebraM[M, F, A, S])(
      scatter: Scatter[F, A, S]
  )(implicit embed: Embed[F, R]): A => M[R] =
    a =>
      coalgebra(a)
        .flatMap(
          _.traverse(
            kernel.hyloM[M, F, S, R](
              embed.algebra.lift[M].apply,
              sa => scatter(sa).fold(coalgebra.apply, _.pure[M]))))
        .map(embed.algebra.apply)

}

private[droste] sealed trait SchemeHyloPorcelain {

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
    kernel.hylo(algebra.apply, coalgebra.apply)

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
    kernel.hyloM(algebra.apply, coalgebra.apply)

}
