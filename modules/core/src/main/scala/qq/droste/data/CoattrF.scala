package qq.droste
package data

import cats.Applicative
import cats.Functor
import cats.Traverse

import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.traverse._

import meta.Meta
import util.DefaultTraverse

object CoattrF {
  def apply [E, W[_], A](f: Either[E, W[A]]): CoattrF[E, W, A] = macro Meta.fastCast
  def un    [E, W[_], A](f: CoattrF[E, W, A]): Either[E, W[A]] = macro Meta.fastCast

  def pure  [E, W[_], A](e: E)    : CoattrF[E, W, A] = CoattrF(Left(e))
  def roll  [E, W[_], A](wa: W[A]): CoattrF[E, W, A] = CoattrF(Right(wa))
}

private[data] trait CoattrFImplicits extends CoenvtTImplicits0 {
  implicit def drosteCoattrFTraverse[E, W[_]: Traverse]: Traverse[CoattrF[E, W, ?]] =
    new CoattrFTraverse[E, W]
}

private[data] sealed trait CoenvtTImplicits0 {
  implicit def drosteCoattrFFunctor[E, W[_]: Functor]: Functor[CoattrF[E, W, ?]] =
    new CoattrFFunctor[E, W]
}

private[data] sealed class CoattrFFunctor[E, W[_]: Functor] extends Functor[CoattrF[E, W, ?]] {
  def map[A, B](fa: CoattrF[E, W, A])(f: A => B): CoattrF[E, W, B] =
    CoattrF.un(fa) match {
      case Right(wa) => CoattrF.roll(wa.map(f))
      case other: Either[E, W[B]] => CoattrF(other)
    }
}

private[data] final class CoattrFTraverse[E, W[_]: Traverse]
    extends CoattrFFunctor[E, W]
    with DefaultTraverse[CoattrF[E, W, ?]]
{
  def traverse[G[_]: Applicative, A, B](fa: CoattrF[E, W, A])(f: A => G[B]): G[CoattrF[E, W, B]] =
    CoattrF.un(fa) match {
      case Right(wa) => wa.traverse(f).map(CoattrF.roll)
      case other: Either[E, W[B]] => CoattrF(other).pure[G]
    }
}
