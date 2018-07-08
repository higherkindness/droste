package qq.droste

import cats.Functor

import data._
import meta.Meta

trait Embed[F[_], R] {
  def algebra: Algebra[F, R]
}

object Embed extends FloatingBasisInstances[Embed] {
  def apply[F[_], R](implicit ev: Embed[F, R]): Embed[F, R] = ev
}

trait Project[F[_], R] {
  def coalgebra: Coalgebra[F, R]
}

object Project extends FloatingBasisInstances[Project] {
  def apply[F[_], R](implicit ev: Project[F, R]): Project[F, R] = ev
}

sealed trait Basis[F[_], R]
    extends Embed[F, R]
    with Project[F, R]

object Basis extends FloatingBasisInstances[Basis] {
  def apply[F[_], R](implicit ev: Basis[F, R]): Basis[F, R] = ev
  final case class Default[F[_], R](
    algebra: Algebra[F, R],
    coalgebra: Coalgebra[F, R]) extends Basis[F, R]
}

private[droste] sealed trait FloatingBasisInstances[H[F[_], A] >: Basis[F, A]] extends FloatingBasisInstances0[H] {
  implicit def drosteBasisForCofree[F[_], E]: H[EnvT[E, F, ?], Cofree[F, E]] =
    Basis.Default[EnvT[E, F, ?], Cofree[F, E]](Cofree.algebra, Cofree.coalgebra)
}

private[droste] sealed trait FloatingBasisInstances0[H[F[_], A] >: Basis[F, A]] {
  implicit def drosteBasisForFix[F[_]]: H[F, Fix[F]] =
    Basis.Default[F, Fix[F]](Fix.algebra, Fix.coalgebra)
}
