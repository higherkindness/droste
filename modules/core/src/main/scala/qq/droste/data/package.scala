package qq.droste
package data

import cats.Eval
import cats.Functor

import cats.syntax.functor._
import meta.Meta

import data.prelude._

object `package` {

  type Fix[F[_]] // = F[Fix[F]]

  object Fix {
    def apply[F[_]](f: F[Fix[F]]): Fix[F]    = macro Meta.fastCast
    def fix  [F[_]](f: F[Fix[F]]): Fix[F]    = macro Meta.fastCast
    def unfix[F[_]](f: Fix[F])   : F[Fix[F]] = macro Meta.fastCast

    def algebra  [F[_]]: Algebra  [F, Fix[F]] = fix(_)
    def coalgebra[F[_]]: Coalgebra[F, Fix[F]] = unfix(_)
  }

  type Cofree[F[_], A] // = (A, F[Cofree[F, A]])

  object Cofree {
    def apply   [F[_], A](head: A, tail: F[Cofree[F, A]]): Cofree[F, A] = apply((head, tail))
    def apply   [F[_], A](f: (A, F[Cofree[F, A]])): Cofree[F, A] = macro Meta.fastCast
    def cofree  [F[_], A](f: (A, F[Cofree[F, A]])): Cofree[F, A] = macro Meta.fastCast
    def uncofree[F[_], A](f: Cofree[F, A]): (A, F[Cofree[F, A]]) = macro Meta.fastCast
    def unapply [F[_], A](f: Cofree[F, A]): Option[(A, F[Cofree[F,A]])] = Some(f.tuple)


    def algebra[E, F[_]]: Algebra[EnvT[E, F, ?], Cofree[F, E]] =
      fa => Cofree(EnvT.unenvT(fa))

    def coalgebra[E, F[_]]: Coalgebra[EnvT[E, F, ?], Cofree[F, E]] =
      a => EnvT(Cofree.uncofree(a))

    def fromCats[F[_]: Functor, A](cofree: cats.free.Cofree[F, A]): Cofree[F, A] =
      Cofree(cofree.head, cofree.tail.value.map(fromCats(_)))

    final class Ops[F[_], A](val cofree: Cofree[F, A]) extends AnyVal {
      def tuple: (A, F[Cofree[F, A]]) = Cofree.uncofree(cofree)
      def head: A = tuple._1
      def tail: F[Cofree[F, A]] = tuple._2

      def toCats(implicit ev: Functor[F]): cats.free.Cofree[F, A] =
        cats.free.Cofree(head, Eval.later(tail.map(_.toCats)))
    }
  }

  type EnvT[E, W[_], A] // = (E, W[A])

  object EnvT {
    def apply  [E, W[_], A](ask: E, lower: W[A]): EnvT[E, W, A] = apply((ask, lower))
    def apply  [E, W[_], A](f: (E, W[A])): EnvT[E, W, A] = macro Meta.fastCast
    def envT   [E, W[_], A](f: (E, W[A])): EnvT[E, W, A] = macro Meta.fastCast
    def unenvT [E, W[_], A](f: EnvT[E, W, A]): (E, W[A]) = macro Meta.fastCast
    def unapply[E, W[_], A](f: EnvT[E, W, A]): Option[(E, W[A])] = Some(f.tuple)

    final class Ops[E, W[_], A](val envT: EnvT[E, W, A]) extends AnyVal {
      def tuple: (E, W[A]) = EnvT.unenvT(envT)
      def ask: E = tuple._1
      def lower: W[A] = tuple._2
    }
  }
}
