package qq.droste

import cats.Applicative
import cats.Functor
import cats.syntax.applicative._
import cats.syntax.functor._

final class GAlgebra[S, F[_], A](val run: F[S] => A) extends AnyVal {
  def apply(fs: F[S]): A = run(fs)

  def zip[T, B](that: GAlgebra[T, F, B])(implicit ev: Functor[F]): GAlgebra[(S, T), F, (A, B)] =
    GAlgebra.zip(this, that)

  def gather(gather: Gather[S, F, A]): GAlgebra.Gathered[S, F, A] =
    GAlgebra.Gathered(this, gather)

  def lift[M[_]](implicit M: Applicative[M]): F[S] => M[A] = fs => run(fs).pure[M]

  def compose[Z](f: F[Z] => F[S]): GAlgebra[Z, F, A] =
    GAlgebra(run compose f)

  def andThen[B](f: A => B): GAlgebra[S, F, B] =
    GAlgebra(run andThen f)
}

object GAlgebra {

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
  def apply(a: A): F[S] = run(a)

  def scatter(scatter: Scatter[S, F, A]): GCoalgebra.Scattered[S, F, A] =
    GCoalgebra.Scattered(this, scatter)

  def lift[M[_]](implicit M: Applicative[M]): A => M[F[S]] = a => run(a).pure[M]

  def compose[Z](f: Z => A): GCoalgebra[S, F, Z] =
    GCoalgebra(run compose f)

  def andThen[T](f: F[S] => F[T]): GCoalgebra[T, F, A] =
    GCoalgebra(run andThen f)
}

object GCoalgebra {

  def apply[S, F[_], A](run: A => F[S]): GCoalgebra[S, F, A] =
    new GCoalgebra(run)

  final case class Scattered[S, F[_], A](
    coalgebra: GCoalgebra[S, F, A],
    scatter: Scatter[S, F, A]
  )
}
