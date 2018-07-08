package qq.droste
package data

import cats.Applicative
import cats.Functor
import cats.Traverse

import cats.syntax.functor._
import cats.syntax.traverse._

package object prelude extends DataPrelude

private[droste] trait DataPrelude extends DataInstances

private[data] sealed trait DataInstances extends DataInstances0 {
  implicit def drosteEnvTTraverse[E, W[_]: Traverse]: Traverse[EnvT[E, W, ?]] =
    new EnvTTraverse[E, W]
}

private[data] sealed trait DataInstances0 {
  implicit def drosteEnvTFunctor[E, W[_]: Functor]: Functor[EnvT[E, W, ?]] =
    new EnvTFunctor[E, W]
}

private[data] class EnvTFunctor[E, W[_]: Functor] extends Functor[EnvT[E, W, ?]] {
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
