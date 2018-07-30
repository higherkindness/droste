package qq.droste
package data

import meta.Meta

object Free {
  def apply[F[_], A](f: Either[A, F[Free[F, A]]]): Free[F, A] = macro Meta.fastCast
  def un   [F[_], A](f: Free[F, A]): Either[A, F[Free[F, A]]] = macro Meta.fastCast

  def pure[F[_], A](a: A): Free[F, A] = apply(Left(a))
  def roll[F[_], A](fa: F[Free[F, A]]): Free[F, A] = apply(Right(fa))

  def algebra[E, F[_]]: Algebra[CoenvT[E, F, ?], Free[F, E]] =
    Algebra(fa => Free(CoenvT.un(fa)))

  def coalgebra[E, F[_]]: Coalgebra[CoenvT[E, F, ?], Free[F, E]] =
    Coalgebra(a => CoenvT(Free.un(a)))
}

trait FreeImplicits {
  implicit final class FreeOps[F[_], A](free: Free[F, A]) {
    def fold[B](f: A => B, fffa: F[Free[F, A]] => B): B =
      Free.un(free).fold(f, fffa)
  }
}
