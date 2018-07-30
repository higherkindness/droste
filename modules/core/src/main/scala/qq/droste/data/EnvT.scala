package qq.droste
package data

import cats.Applicative
import cats.Functor
import cats.Traverse

import cats.syntax.functor._
import cats.syntax.traverse._

import meta.Meta
import data.prelude._
import util.DefaultTraverse

object EnvT {
  def apply  [E, W[_], A](ask: E, lower: W[A]): EnvT[E, W, A] = apply((ask, lower))
  def apply  [E, W[_], A](f: (E, W[A])): EnvT[E, W, A] = macro Meta.fastCast
  def un     [E, W[_], A](f: EnvT[E, W, A]): (E, W[A]) = macro Meta.fastCast
  def unapply[E, W[_], A](f: EnvT[E, W, A]): Option[(E, W[A])] = Some(f.tuple)
}

private[data] trait EnvTImplicits extends EnvTImplicits0 {
  implicit final class EnvTOps[E, W[_], A](envT: EnvT[E, W, A]) {
    def tuple: (E, W[A]) = EnvT.un(envT)
    def ask: E = tuple._1
    def lower: W[A] = tuple._2
  }

  implicit def drosteEnvTTraverse[E, W[_]: Traverse]: Traverse[EnvT[E, W, ?]] =
    new EnvTTraverse[E, W]
}

private[data] sealed trait EnvTImplicits0 {
  implicit def drosteEnvTFunctor[E, W[_]: Functor]: Functor[EnvT[E, W, ?]] =
    new EnvTFunctor[E, W]
}


private[data] sealed class EnvTFunctor[E, W[_]: Functor] extends Functor[EnvT[E, W, ?]] {
  def map[A, B](fa: EnvT[E, W, A])(f: A => B): EnvT[E, W, B] =
    EnvT(fa.ask, fa.lower.map(f))
}

private[data] final class EnvTTraverse[E, W[_]: Traverse]
    extends EnvTFunctor[E, W]
    with DefaultTraverse[EnvT[E, W, ?]]
{
  def traverse[G[_]: Applicative, A, B](fa: EnvT[E, W, A])(f: A => G[B]): G[EnvT[E, W, B]] =
    fa.lower.traverse(f).map(EnvT(fa.ask, _))
}
