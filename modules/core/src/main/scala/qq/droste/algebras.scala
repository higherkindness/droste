package qq.droste

import cats.Applicative
import cats.CoflatMap
import cats.Comonad
import cats.FlatMap
import cats.Functor
import cats.Monad
import cats.arrow.Arrow
import cats.data.Cokleisli
import cats.data.Kleisli
import cats.syntax.applicative._
import cats.syntax.coflatMap._
import cats.syntax.comonad._
import cats.syntax.flatMap._
import cats.syntax.functor._

final class GAlgebra[S, F[_], A](val run: F[S] => A) extends AnyVal {
  def apply(fs: F[S]): A =
    run(fs)

  def zip[T, B](that: GAlgebra[T, F, B])(implicit ev: Functor[F]): GAlgebra[(S, T), F, (A, B)] =
    GAlgebra.zip(this, that)

  def gather(gather: Gather[S, F, A]): GAlgebra.Gathered[S, F, A] =
    GAlgebra.Gathered(this, gather)

  def lift[M[_]](implicit M: Applicative[M]): F[S] => M[A] = fs => run(fs).pure[M]

  def compose[Z](f: GAlgebra[Z, F, S])(implicit F: CoflatMap[F]): GAlgebra[Z, F, A] =
    GAlgebra(fz => run(fz coflatMap f.run))

  def andThen[B](f: GAlgebra[A, F, B])(implicit F: CoflatMap[F]): GAlgebra[S, F, B] =
    f compose this

  def andThenRun[B](f: A => B): GAlgebra[S, F, B] =
    GAlgebra(fs => f(run(fs)))

  def toCokleisli: Cokleisli[F, S, A] =
    Cokleisli(run)
}

object GAlgebra extends GAlgebraInstances {

  def apply[S, F[_], A](run: F[S] => A): GAlgebra[S, F, A] =
    new GAlgebra(run)

  def zip[F[_]: Functor, Sx, Sy, Ax, Ay](
    x: GAlgebra[Sx, F, Ax],
    y: GAlgebra[Sy, F, Ay]
  ): GAlgebra[(Sx, Sy), F, (Ax, Ay)] =
    GAlgebra(fz => (
      x(fz.map(v => v._1)),
      y(fz.map(v => v._2))))

  final case class Gathered[S, F[_], A](
    algebra: GAlgebra[S, F, A],
    gather: Gather[S, F, A]
  ) {
    def zip[B, T](that: Gathered[T, F, B])(implicit ev: Functor[F]): Gathered[(S, T), F, (A, B)] =
      Gathered(
        GAlgebra.zip(algebra, that.algebra),
        Gather.zip(gather, that.gather))
  }
}

final class GCoalgebra[S, F[_], A](val run: A => F[S]) extends AnyVal {
  def apply(a: A): F[S] =
    run(a)

  def scatter(scatter: Scatter[S, F, A]): GCoalgebra.Scattered[S, F, A] =
    GCoalgebra.Scattered(this, scatter)

  def lift[M[_]](implicit M: Applicative[M]): A => M[F[S]] =
    a => run(a).pure[M]

  def compose[Z](f: GCoalgebra[A, F, Z])(implicit F: FlatMap[F]): GCoalgebra[S, F, Z] =
    GCoalgebra(z => f(z) flatMap run)

  def andThen[T](f: GCoalgebra[T, F, S])(implicit F: FlatMap[F]): GCoalgebra[T, F, A] =
    f compose this

  def toKleisli: Kleisli[F, A, S] =
    Kleisli(run)
}

object GCoalgebra extends GCoalgebraInstances {
  def apply[S, F[_], A](run: A => F[S]): GCoalgebra[S, F, A] =
    new GCoalgebra(run)

  final case class Scattered[S, F[_], A](
    coalgebra: GCoalgebra[S, F, A],
    scatter: Scatter[S, F, A]
  )
}

// instances

private[droste] sealed trait GAlgebraInstances {

  implicit def drosteArrowForGAlgebra[F[_]: Comonad]: Arrow[GAlgebra[?, F, ?]] =
    new GAlgebraArrow
}

private[droste] class GAlgebraArrow[F[_]: Comonad] extends Arrow[GAlgebra[?, F, ?]] {
  def lift[A, B](f: A => B): GAlgebra[A, F, B] =
    GAlgebra(fa => f(fa.extract))
  def compose[A, B, C](f: GAlgebra[B, F, C], g: GAlgebra[A, F, B]): GAlgebra[A, F, C] =
    f compose g
  override def andThen[A, B, C](f: GAlgebra[A, F, B], g: GAlgebra[B, F, C]): GAlgebra[A, F, C] =
    f andThen g
  def first[A, B, C](f: GAlgebra[A, F, B]): GAlgebra[(A, C), F, (B, C)] =
    GAlgebra(fac => (f.run(fac.map(_._1)), fac.map(_._2).extract))
}

private[droste] sealed trait GCoalgebraInstances {
  // Note: It's very difficult to summon this instance since it's
  // right-to-left instead of left-to-right. So...
  // TODO: Re-evaluate order of type parameters for algebras
  implicit def drosteArrowForGCoalgebra[F[_]: Monad]: Arrow[λ[(α, β) => GCoalgebra[β, F, α]]] =
    new GCoalgebraArrow
}

private[droste] class GCoalgebraArrow[F[_]: Monad]
    extends Arrow[λ[(α, β) => GCoalgebra[β, F, α]]]
{
  def lift[A, B](f: A => B): GCoalgebra[B, F, A] =
    GCoalgebra(a => f(a).pure[F])
  def compose[A, B, C](f: GCoalgebra[C, F, B], g: GCoalgebra[B, F, A]): GCoalgebra[C, F, A] =
    f compose g
  override def andThen[A, B, C](f: GCoalgebra[B, F, A], g: GCoalgebra[C, F, B]): GCoalgebra[C, F, A] =
    f andThen g
  def first[A, B, C](fa: GCoalgebra[B, F, A]): GCoalgebra[(B, C), F, (A, C)] =
    GCoalgebra(ac => fa.run(ac._1).fproduct(_ => ac._2))
}
