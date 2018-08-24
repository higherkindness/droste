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

object AttrF {
  def apply  [E, W[_], A](ask: E, lower: W[A]): AttrF[E, W, A] = apply((ask, lower))
  def apply  [E, W[_], A](f: (E, W[A])): AttrF[E, W, A] = macro Meta.fastCast
  def un     [E, W[_], A](f: AttrF[E, W, A]): (E, W[A]) = macro Meta.fastCast
  def unapply[E, W[_], A](f: AttrF[E, W, A]): Option[(E, W[A])] = Some(f.tuple)
}

private[data] trait AttrFImplicits extends AttrFImplicits0 {
  implicit final class AttrFOps[E, W[_], A](attrf: AttrF[E, W, A]) {
    def tuple: (E, W[A]) = AttrF.un(attrf)
    def ask: E = tuple._1
    def lower: W[A] = tuple._2
  }

  implicit def drosteAttrFTraverse[E, W[_]: Traverse]: Traverse[AttrF[E, W, ?]] =
    new AttrFTraverse[E, W]
}

private[data] sealed trait AttrFImplicits0 {
  implicit def drosteAttrFFunctor[E, W[_]: Functor]: Functor[AttrF[E, W, ?]] =
    new AttrFFunctor[E, W]
}


private[data] sealed class AttrFFunctor[E, W[_]: Functor] extends Functor[AttrF[E, W, ?]] {
  def map[A, B](fa: AttrF[E, W, A])(f: A => B): AttrF[E, W, B] =
    AttrF(fa.ask, fa.lower.map(f))
}

private[data] final class AttrFTraverse[E, W[_]: Traverse]
    extends AttrFFunctor[E, W]
    with DefaultTraverse[AttrF[E, W, ?]]
{
  def traverse[G[_]: Applicative, A, B](fa: AttrF[E, W, A])(f: A => G[B]): G[AttrF[E, W, B]] =
    fa.lower.traverse(f).map(AttrF(fa.ask, _))
}
