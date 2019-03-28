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

/** A `GAlgebra[F[_], S, A]` is a value-class wrapper for a function from `F[S]` to `A`.
  *
  * This type is isomorphic to a `cats.data.Cokleisli[F, S, A]`.
  */
final class GAlgebra[F[_], S, A](val run: F[S] => A) extends AnyVal {
  def apply(fs: F[S]): A =
    run(fs)

  def zip[T, B](that: GAlgebra[F, T, B])(
      implicit ev: Functor[F]): GAlgebra[F, (S, T), (A, B)] =
    GAlgebra.zip(this, that)

  def gather(gather: Gather[F, S, A]): GAlgebra.Gathered[F, S, A] =
    GAlgebra.Gathered(this, gather)

  /**
    * Lifts the result `A` of this `GAlgebra` into a pure `M[A]`.
    *
    * Removing wrappers, the time of this method comes down to the following:
    * `(F[S] => A) => (α => M[α]) => (F[S] => M[A])`,
    * where the α is the universally quantified `p`
    */
  def lift[M[_]](implicit M: Applicative[M]): GAlgebraM[M, F, S, A] =
    GAlgebraM(fs => run(fs).pure[M])

  /**
    * Removing wrappers, the type (F[S] => A) => (F[Z] => S) => (F[Z] => A)
    *
    * This operation is equivalent to the `compose` method of `Cokleisli`.
    */
  def compose[Z](f: GAlgebra[F, Z, S])(
      implicit F: CoflatMap[F]): GAlgebra[F, Z, A] =
    GAlgebra(fz => run(fz coflatMap f.run))

  def andThen[B](f: GAlgebra[F, A, B])(
      implicit F: CoflatMap[F]): GAlgebra[F, S, B] =
    f compose this

  def toCokleisli: Cokleisli[F, S, A] =
    Cokleisli(run)
}

object GAlgebra extends GAlgebraInstances {

  def apply[F[_], S, A](run: F[S] => A): GAlgebra[F, S, A] =
    new GAlgebra(run)

  /** This operation is equivalent to the `split` operation from the instance of the `cats.Arrow`
    * typeclass for `Cokleisli`
    */
  def zip[F[_]: Functor, Sx, Sy, Ax, Ay](
      x: GAlgebra[F, Sx, Ax],
      y: GAlgebra[F, Sy, Ay]
  ): GAlgebra[F, (Sx, Sy), (Ax, Ay)] =
    GAlgebra(fz => (x(fz.map(v => v._1)), y(fz.map(v => v._2))))

  /**
    * A GAlgebra.Gathered[F[_], S, A]` is a pair of two functions, wrapped in value classes.
    * - An `Algebra[F, S, A]`, which is to say a wrapper for `F[S] => A`.
    * - A  `Gather[F, S, A]`, that is a wrapper for `(A, F[S]) => S`
    */
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

/** A `GAlgebraM[F[_], S, A]` is a wrapper (value-class) for a function from `F[S]` to `M[A]`.
  *
  * This type is similar to `GAlgebra[F, S, M[A]]`; vice-versa,
  * a `GAlgebra[F, S, A]` is similar to a `GAlgebraM[Id, F, S, A]`.
  */
final class GAlgebraM[M[_], F[_], S, A](val run: F[S] => M[A]) extends AnyVal {
  def apply(fs: F[S]): M[A] =
    run(fs)

  def zip[T, B](that: GAlgebraM[M, F, T, B])(
      implicit M: Semigroupal[M],
      F: Functor[F]
  ): GAlgebraM[M, F, (S, T), (A, B)] =
    GAlgebraM.zip(this, that)

  def gather(gather: Gather[F, S, A]): GAlgebraM.Gathered[M, F, S, A] =
    GAlgebraM.Gathered(this, gather)
}

object GAlgebraM {
  def apply[M[_], F[_], S, A](run: F[S] => M[A]): GAlgebraM[M, F, S, A] =
    new GAlgebraM(run)

  def zip[M[_]: Semigroupal, F[_]: Functor, Sx, Sy, Ax, Ay](
      x: GAlgebraM[M, F, Sx, Ax],
      y: GAlgebraM[M, F, Sy, Ay]
  ): GAlgebraM[M, F, (Sx, Sy), (Ax, Ay)] =
    GAlgebraM(fz => x(fz.map(v => v._1)) product y(fz.map(v => v._2)))

  final case class Gathered[M[_], F[_], S, A](
      algebra: GAlgebraM[M, F, S, A],
      gather: Gather[F, S, A]
  ) {
    def zip[B, T](that: Gathered[M, F, T, B])(
        implicit M: Semigroupal[M],
        F: Functor[F]
    ): Gathered[M, F, (S, T), (A, B)] =
      Gathered(
        GAlgebraM.zip(algebra, that.algebra),
        Gather.zip(gather, that.gather))
  }
}

/** A GCoalgebra[F[_], A, S] is a value-class wrapper around a function `A => F[S]`.
  *
  * This type is isomorphic to (the same as) a `cats.data.Kleisli[F, A, S]`.
  * However, here we use different methods to those of the `Kleisli` class.
  */
final class GCoalgebra[F[_], A, S](val run: A => F[S]) extends AnyVal {
  def apply(a: A): F[S] =
    run(a)

  def scatter(scatter: Scatter[F, A, S]): GCoalgebra.Scattered[F, A, S] =
    GCoalgebra.Scattered(this, scatter)

  /**
    * Removing all middle wrappers, the type of this `lift` becomes:
    * `(A => F[S]) =>  (α => M[α]) => ( A => M[F[S]] )
    */
  def lift[M[_]](implicit M: Applicative[M]): GCoalgebraM[M, F, A, S] =
    GCoalgebraM(a => run(a).pure[M])

  def compose[Z](f: GCoalgebra[F, Z, A])(
      implicit F: FlatMap[F]): GCoalgebra[F, Z, S] =
    GCoalgebra(z => f(z) flatMap run)

  def andThen[T](f: GCoalgebra[F, S, T])(
      implicit F: FlatMap[F]): GCoalgebra[F, A, T] =
    f compose this

  def toKleisli: Kleisli[F, A, S] =
    Kleisli(run)
}

object GCoalgebra extends GCoalgebraInstances {
  def apply[F[_], A, S](run: A => F[S]): GCoalgebra[F, A, S] =
    new GCoalgebra(run)

  /** A `Scattered[F, A, S]` is a wrapper around a pair of functions:
    * - A `Coalgebra[F, A, S]`, which comes down to `A => F[S]`
    * - A `Scatter[F, A, S]`, which can be reduced to `S => Either[A, F[S]]`
    */
  final case class Scattered[F[_], A, S](
      coalgebra: GCoalgebra[F, A, S],
      scatter: Scatter[F, A, S]
  )
}

final class GCoalgebraM[M[_], F[_], A, S](val run: A => M[F[S]])
    extends AnyVal {
  def apply(a: A): M[F[S]] =
    run(a)

  def scatter(scatter: Scatter[F, A, S]): GCoalgebraM.Scattered[M, F, A, S] =
    GCoalgebraM.Scattered(this, scatter)
}

object GCoalgebraM {
  def apply[M[_], F[_], S, A](run: A => M[F[S]]): GCoalgebraM[M, F, A, S] =
    new GCoalgebraM(run)

  final case class Scattered[M[_], F[_], A, S](
      coalgebra: GCoalgebraM[M, F, A, S],
      scatter: Scatter[F, A, S]
  )
}

// instances

private[droste] sealed trait GAlgebraInstances {

  implicit def drosteArrowForGAlgebra[F[_]: Comonad]: Arrow[GAlgebra[F, ?, ?]] =
    new GAlgebraArrow
}

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
    GAlgebra(fac => (f.run(fac.map(_._1)), fac.map(_._2).extract))
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
    GCoalgebra(ac => fa.run(ac._1).fproduct(_ => ac._2))
}
