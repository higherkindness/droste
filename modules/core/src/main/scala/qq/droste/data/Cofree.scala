package qq.droste
package data

import cats.Comonad
import cats.Eval
import cats.Functor

import cats.syntax.functor._

import meta.Meta
import prelude._

object Cofree {
  def apply  [F[_], A](head: A, tail: F[Cofree[F, A]]): Cofree[F, A] = apply((head, tail))
  def apply  [F[_], A](f: (A, F[Cofree[F, A]])): Cofree[F, A] = macro Meta.fastCast
  def un     [F[_], A](f: Cofree[F, A]): (A, F[Cofree[F, A]]) = macro Meta.fastCast
  def unapply[F[_], A](f: Cofree[F, A]): Option[(A, F[Cofree[F,A]])] = Some(f.tuple)

  def algebra[E, F[_]]: Algebra[EnvT[E, F, ?], Cofree[F, E]] =
    Algebra(fa => Cofree(EnvT.un(fa)))

  def coalgebra[E, F[_]]: Coalgebra[EnvT[E, F, ?], Cofree[F, E]] =
    Coalgebra(a => EnvT(Cofree.un(a)))

  def fromCats[F[_]: Functor, A](cofree: cats.free.Cofree[F, A]): Cofree[F, A] =
    ana(cofree)(_.tail.value, _.head)

  def unfold[F[_]: Functor, A](a: A)(coalgebra: A => F[A]): Cofree[F, A] =
    ana(a)(coalgebra, identity)

  /** An inlined anamorphism to `Cofree` with a fused map */
  def ana[F[_]: Functor, A, C](a: A)(coalgebra: A => F[A], f: A => C): Cofree[F, C] =
    Cofree(f(a), coalgebra(a).map(ana(_)(coalgebra, f)))
}

private[data] trait CofreeImplicits {
  implicit final class CofreeOps[F[_], A](cofree: Cofree[F, A]) {
    def tuple: (A, F[Cofree[F, A]]) = Cofree.un(cofree)
    def head: A = tuple._1
    def tail: F[Cofree[F, A]] = tuple._2

    def toCats(implicit ev: Functor[F]): cats.free.Cofree[F, A] =
      cats.free.Cofree(head, Eval.later(tail.map(_.toCats)))

    def forget(implicit ev: Functor[F]): Fix[F] =
      Fix(tail.map(_.forget))
  }

  implicit def drosteCofreeComonad[F[_]: Functor]: Comonad[Cofree[F, ?]] =
    new CofreeComonad[F]
}

private[data] final class CofreeComonad[F[_]: Functor] extends Comonad[Cofree[F, ?]] {
  def coflatMap[A, B](fa: Cofree[F, A])(f: Cofree[F, A] => B): Cofree[F, B] =
    Cofree.ana(fa)(_.tail, f)

  def extract[A](fa: Cofree[F, A]): A = fa.head

  def map[A, B](fa: Cofree[F, A])(f: A => B): Cofree[F, B] = Cofree(f(fa.head), fa.tail.map(_.map(f)))
}
