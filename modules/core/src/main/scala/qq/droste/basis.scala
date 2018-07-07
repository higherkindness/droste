package qq.droste

import cats.Functor

import data._
import meta.Meta

trait Embed[F[_], R] {
  def algebra: Algebra[F, R]
}

object Embed {
  implicit def embedFromBasis[F[_], R](implicit f: Basis[F, R]): Embed[F, R] = macro Meta.fastCast
}

trait Project[F[_], R] {
  def coalgebra: Coalgebra[F, R]
}

object Project {
  implicit def projectFromBasis[F[_], R](implicit f: Basis[F, R]): Project[F, R] = macro Meta.fastCast
}

sealed trait Basis[F[_], R]
    extends Embed[F, R]
    with Project[F, R]

object Basis extends BasisInstances0 {
  final case class Default[F[_], R](
    algebra: Algebra[F, R],
    coalgebra: Coalgebra[F, R]) extends Basis[F, R]
}

private[droste] sealed trait BasisInstances0 extends BasisInstances1 {
  implicit def cofree[F[_], E]: Basis[EnvT[E, F, ?], Cofree[F, E]] =
    Basis.Default[EnvT[E, F, ?], Cofree[F, E]](Cofree.algebra, Cofree.coalgebra)
}

private[droste] sealed trait BasisInstances1 extends BasisInstances2 {
  implicit def fix[F[_]]: Basis[F, Fix[F]] =
    Basis.Default[F, Fix[F]](Fix.algebra, Fix.coalgebra)
}

private[droste] sealed trait BasisInstances2 {
  implicit def mu[F[_]: Functor]: Basis[F, Mu[F]] =
    Basis.Default[F, Mu[F]](Mu.algebra, Mu.coalgebra)

  implicit def nu[F[_]: Functor]: Basis[F, Nu[F]] =
    Basis.Default[F, Nu[F]](Nu.algebra, Nu.coalgebra)
}
