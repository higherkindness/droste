package qq.droste
package data

import meta.Meta

object Coattr {
  def apply[F[_], A](f: Either[A, F[Coattr[F, A]]]): Coattr[F, A] = macro Meta.fastCast
  def un   [F[_], A](f: Coattr[F, A]): Either[A, F[Coattr[F, A]]] = macro Meta.fastCast

  def pure[F[_], A](a: A): Coattr[F, A] = apply(Left(a))
  def roll[F[_], A](fa: F[Coattr[F, A]]): Coattr[F, A] = apply(Right(fa))

  def algebra[E, F[_]]: Algebra[CoattrF[E, F, ?], Coattr[F, E]] =
    Algebra(fa => Coattr(CoattrF.un(fa)))

  def coalgebra[E, F[_]]: Coalgebra[CoattrF[E, F, ?], Coattr[F, E]] =
    Coalgebra(a => CoattrF(Coattr.un(a)))
}

trait CoattrImplicits {
  implicit final class CoattrOps[F[_], A](coattr: Coattr[F, A]) {
    def fold[B](f: A => B, fffa: F[Coattr[F, A]] => B): B =
      Coattr.un(coattr).fold(f, fffa)
  }
}
