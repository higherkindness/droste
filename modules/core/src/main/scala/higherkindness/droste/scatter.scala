package higherkindness.droste

import cats.Functor
import cats.syntax.functor._

/** Companion object for `Scatter[F[_], A, S]`, which is a type alias of `S => Either[A, F[S]]`
  */
object Scatter {

  /** Returns a Scatterer that lifts the input `A` into a `Left` */
  def ana[F[_], A]: Scatter[F, A, A] =
    Left(_)

  /**
    * Result type is a `Scatter[F, A, Either[B, A]]`, which is equivalent to
    * a function `Either[B, A] => Either[A, F[Either[B, A]]]`
    */
  def gapo[F[_]: Functor, A, B](
      coalgebra: Coalgebra[F, B]): Scatter[F, A, Either[B, A]] = {
    case Left(b)  => Right(coalgebra(b).map(Left(_)))
    case Right(a) => Left(a)
  }

  /**
    * The result type comes down to a wrapper of `Either[B, A] => Either[A, F[Either[B, A]]]`.
    */
  def apo[F[_]: Functor, A, B](
      implicit project: Project[F, B]): Scatter[F, A, Either[B, A]] =
    gapo(project.coalgebra)
}
