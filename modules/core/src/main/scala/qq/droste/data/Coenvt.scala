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

object CoenvT {
  def apply [E, W[_], A](f: Either[E, W[A]]): CoenvT[E, W, A] = macro Meta.fastCast
  def un    [E, W[_], A](f: CoenvT[E, W, A]): Either[E, W[A]] = macro Meta.fastCast

  def pure  [E, W[_], A](e: E)    : CoenvT[E, W, A] = CoenvT(Left(e))
  def roll  [E, W[_], A](wa: W[A]): CoenvT[E, W, A] = CoenvT(Right(wa))
}

private[data] trait CoenvTImplicits extends CoenvtTImplicits0 {
  implicit def drosteCoenvTTraverse[E, W[_]: Traverse]: Traverse[CoenvT[E, W, ?]] =
    new CoenvTTraverse[E, W]
}

private[data] sealed trait CoenvtTImplicits0 {
  implicit def drosteCoenvTFunctor[E, W[_]: Functor]: Functor[CoenvT[E, W, ?]] =
    new CoenvTFunctor[E, W]
}

private[data] sealed class CoenvTFunctor[E, W[_]: Functor] extends Functor[CoenvT[E, W, ?]] {
  def map[A, B](fa: CoenvT[E, W, A])(f: A => B): CoenvT[E, W, B] =
    CoenvT.un(fa) match {
      case Right(wa) => CoenvT.roll(wa.map(f))
      case other: Either[E, W[B]] => CoenvT(other)
    }
}

private[data] final class CoenvTTraverse[E, W[_]: Traverse]
    extends CoenvTFunctor[E, W]
    with DefaultTraverse[CoenvT[E, W, ?]]
{
  def traverse[G[_]: Applicative, A, B](fa: CoenvT[E, W, A])(f: A => G[B]): G[CoenvT[E, W, B]] =
    CoenvT.un(fa) match {
      case Right(wa) => wa.traverse(f).map(CoenvT.roll)
      case other: Either[E, W[B]] => CoenvT(other).pure[G]
    }
}
