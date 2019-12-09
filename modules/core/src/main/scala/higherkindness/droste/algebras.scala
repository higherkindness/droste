package higherkindness.droste

import cats.Applicative
import cats.CoflatMap
import cats.Comonad
import cats.FlatMap
import cats.Functor
import cats.Monad
import cats.Semigroupal
import cats.arrow.Arrow
import cats.data.Cokleisli
import cats.data.Kleisli
import cats.syntax.applicative._
import cats.syntax.coflatMap._
import cats.syntax.comonad._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.semigroupal._

/** Wraps a function from F[S] to A.
  *
  * This type is similar to cats.data.Cokleisli
  */
abstract class GAlgebra[F[_], S, A] { self =>
  def apply(fs: F[S]): A

  def zip[T, B](that: GAlgebra[F, T, B])(
      implicit ev: Functor[F]): GAlgebra[F, (S, T), (A, B)] =
    GAlgebra.zip(this, that)

  def gather(gather: Gather[F, S, A]): GAlgebra.Gathered[F, S, A] =
    GAlgebra.Gathered(this, gather)

  /** Lifts this GAlgebra into a GAlgebraM on the M[_]  Applicative,
    by mapping every output values to pure M values. */
  def lift[M[_]](implicit M: Applicative[M]): GAlgebraM[M, F, S, A] =
    new GAlgebraM {
      def apply(fs: F[S]): M[A] = M.pure(self(fs))
    }

  def compose[Z](f: GAlgebra[F, Z, S])(
      implicit F: CoflatMap[F]): GAlgebra[F, Z, A] =
    new GAlgebra[F, Z, A]{
      def apply(fz: F[Z]): A = self(fz.coflatMap(f.apply))
    }

  def andThen[B](f: GAlgebra[F, A, B])(
      implicit F: CoflatMap[F]): GAlgebra[F, S, B] =
    f compose this

  def toCokleisli: Cokleisli[F, S, A] =
    Cokleisli(apply(_))
}

object GAlgebra extends GAlgebraInstances {

  def apply[F[_], S, A](run: F[S] => A): GAlgebra[F, S, A] =
    new GAlgebra[F, S, A] {
      def apply(fs: F[S]): A = run(fs)
    }

  def zip[F[_]: Functor, Sx, Sy, Ax, Ay](
      x: GAlgebra[F, Sx, Ax],
      y: GAlgebra[F, Sy, Ay]
  ): GAlgebra[F, (Sx, Sy), (Ax, Ay)] =
    new GAlgebra {
      def apply(fz: F[(Sx, Sy)]): (Ax, Ay) =
        (x(fz.map(v => v._1)) -> y(fz.map(v => v._2)))
    }

  final case class Gathered[F[_], S, A](
      algebra: GAlgebra[F, S, A],
      gather: Gather[F, S, A]
  ) {
    def zip[B, T](that: Gathered[F, T, B])(
        implicit ev: Functor[F]): Gathered[F, (S, T), (A, B)] =
      Gathered(
        GAlgebra.zip(algebra, that.algebra),
        Gather.zip(gather, that.gather))
  }
}

/** Wraps a function from F[S] to a M[A]  */
abstract class GAlgebraM[M[_], F[_], S, A] { self =>
  def apply(fs: F[S]): M[A]

  def zip[T, B](that: GAlgebraM[M, F, T, B])(
      implicit M: Semigroupal[M],
      F: Functor[F]
  ): GAlgebraM[M, F, (S, T), (A, B)] =
    GAlgebraM.zip(this, that)

  def gather(gather: Gather[F, S, A]): GAlgebraM.Gathered[M, F, S, A] =
    GAlgebraM.Gathered(this, gather)
}

object GAlgebraM {

  /**
    */
  def apply[M[_], F[_], S, A](run: F[S] => M[A]): GAlgebraM[M, F, S, A] =
    new GAlgebraM[M, F, S, A] {
      def apply(fs: F[S]): M[A] = run(fs)
    }

  def zip[M[_]: Semigroupal, F[_]: Functor, Sx, Sy, Ax, Ay](
      x: GAlgebraM[M, F, Sx, Ax],
      y: GAlgebraM[M, F, Sy, Ay]
  ): GAlgebraM[M, F, (Sx, Sy), (Ax, Ay)] =
    GAlgebraM(fz => x(fz.map(v => v._1)) product y(fz.map(v => v._2)))

  abstract class Gathered[M[_], F[_], S, A] extends GAlgebraM[M, F, S, A]{
    def gather(a: A, fs: F[S]): S

    def zip[B, T](that: Gathered[M, F, T, B])(
        implicit M: Semigroupal[M],
        F: Functor[F]
    ): Gathered[M, F, (S, T), (A, B)] =
      new Gathered[M, F, (S, T), (A, B)]  {
        def apply(x: X)

        def gather(ab: (A, B), fst: F[(S, T)]): (S, T) =
          Gather.zip(gather, that.gather)
      }
  }
}

abstract class GCoalgebra[F[_], A, S] { self =>
  def apply(a: A): F[S]

  def scatter(scatter: Scatter[F, A, S]): GCoalgebra.Scattered[F, A, S] =
    GCoalgebra.Scattered(this, scatter)

  def lift[M[_]](implicit M: Applicative[M]): GCoalgebraM[M, F, A, S] =
    GCoalgebraM(a => self(a).pure[M])

  def compose[Z](f: GCoalgebra[F, Z, A])(
      implicit F: FlatMap[F]): GCoalgebra[F, Z, S] =
    GCoalgebra(z => f(z) flatMap apply)

  def andThen[T](f: GCoalgebra[F, S, T])(
      implicit F: FlatMap[F]): GCoalgebra[F, A, T] =
    f compose this

  def toKleisli: Kleisli[F, A, S] =
    Kleisli(apply)
}

object GCoalgebra extends GCoalgebraInstances {
  def apply[F[_], A, S](run: A => F[S]): GCoalgebra[F, A, S] =
    new GCoalgebra[F, A, S] {
      def apply(a: A): F[S] = run(a)
    }

  abstract class Scattered[F[_], A, S] extends GCoalgebra[F, A, S]{
    def scatter(s: S): Either[A, F[S]]
  }

}

abstract class GCoalgebraM[M[_], F[_], A, S] {
  def apply(a: A): M[F[S]]

  def scatter(scatter: Scatter[F, A, S]): GCoalgebraM.Scattered[M, F, A, S] =
    GCoalgebraM.Scattered(this, scatter)
}

object GCoalgebraM {
  def apply[M[_], F[_], S, A](run: A => M[F[S]]): GCoalgebraM[M, F, A, S] =
    new GCoalgebraM[M, F, A, S] {
      def apply(a: A): M[F[S]] = run(a)
    }

  abstract class Scattered[M[_], F[_], A, S] extends GCoalgebraM[M, F, A, S] {
    def scatter(s: S): Either[A, F[S]]
  }
}

// instances

private[droste] sealed trait GAlgebraInstances {

  implicit def drosteArrowForGAlgebra[F[_]: Comonad]: Arrow[GAlgebra[F, ?, ?]] =
    new GAlgebraArrow
}

/*
 * For every F[_] that has an instance of Comonad, the type GAlgebra[F, ?, ?]
 * admits an instance of the Arrow type-class that we can define on top of it.
 *
 * This instance is coherent with the instance of Arrow provided by cats for Cokleisli.
 */
private[droste] class GAlgebraArrow[F[_]: Comonad]
    extends Arrow[GAlgebra[F, ?, ?]] {
  def lift[A, B](f: A => B): GAlgebra[F, A, B] =
    GAlgebra(fa => f(fa.extract))
  def compose[A, B, C](
      f: GAlgebra[F, B, C],
      g: GAlgebra[F, A, B]): GAlgebra[F, A, C] =
    f compose g
  override def andThen[A, B, C](
      f: GAlgebra[F, A, B],
      g: GAlgebra[F, B, C]): GAlgebra[F, A, C] =
    f andThen g
  def first[A, B, C](f: GAlgebra[F, A, B]): GAlgebra[F, (A, C), (B, C)] =
    new GAlgebra[F, (A, C), (B, C)]{
      def apply(in: F[(A, C)] ): (B, C) =
        f(in.map(_._1)) -> in.extract._2
    }
}

private[droste] sealed trait GCoalgebraInstances {
  implicit def drosteArrowForGCoalgebra[F[_]: Monad]: Arrow[
    GCoalgebra[F, ?, ?]] =
    new GCoalgebraArrow
}

private[droste] class GCoalgebraArrow[F[_]: Monad]
    extends Arrow[GCoalgebra[F, ?, ?]] {
  def lift[A, B](f: A => B): GCoalgebra[F, A, B] =
    GCoalgebra(a => f(a).pure[F])
  def compose[A, B, C](
      f: GCoalgebra[F, B, C],
      g: GCoalgebra[F, A, B]): GCoalgebra[F, A, C] =
    f compose g
  override def andThen[A, B, C](
      f: GCoalgebra[F, A, B],
      g: GCoalgebra[F, B, C]): GCoalgebra[F, A, C] =
    f andThen g
  def first[A, B, C](fa: GCoalgebra[F, A, B]): GCoalgebra[F, (A, C), (B, C)] =
    GCoalgebra(ac => fa(ac._1).fproduct(_ => ac._2))
}
