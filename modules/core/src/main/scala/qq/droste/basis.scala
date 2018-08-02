package qq.droste

import data.prelude._
import data.Cofree
import data.CoenvT
import data.EnvT
import data.Free
import data.Fix

import cats.Eval

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

  sealed trait Solve[PR[_[_]]] {
    type PatF[F[_], A]
    type PatR[F[_]] = PR[F]
  }

  object Solve extends FloatingBasisSolveInstances {
    type Aux[PR[_[_]], PF[_[_], _]] = Solve[PR] {
      type PatF[F[_], A] = PF[F, A]
    }
  }
}

private[droste] sealed trait FloatingBasisInstances[H[F[_], A] >: Basis[F, A]] extends FloatingBasisInstances0[H] {
  implicit def drosteBasisForCofree[F[_], E]: H[EnvT[E, F, ?], Cofree[F, E]] =
    Basis.Default[EnvT[E, F, ?], Cofree[F, E]](Cofree.algebra, Cofree.coalgebra)

  implicit def drosteBasisForFree[F[_], E]: H[CoenvT[E, F, ?], Free[F, E]] =
    Basis.Default[CoenvT[E, F, ?], Free[F, E]](Free.algebra, Free.coalgebra)
}

private[droste] sealed trait FloatingBasisInstances0[H[F[_], A] >: Basis[F, A]] {
  implicit def drosteBasisForFix[F[_]]: H[F, Fix[F]] =
    Basis.Default[F, Fix[F]](Fix.algebra, Fix.coalgebra)

  implicit def drosteBasisForCatsCofree[F[_], E]: H[EnvT[E, F, ?], cats.free.Cofree[F, E]] =
    Basis.Default[EnvT[E, F, ?], cats.free.Cofree[F, E]](
      Algebra(fa => cats.free.Cofree(fa.ask, Eval.now(fa.lower))),
      Coalgebra(a => EnvT(a.head, a.tailForced)))
}

private[droste] sealed trait FloatingBasisSolveInstances {
  import Basis.Solve

  implicit val drosteSolveFix: Solve.Aux[Fix, λ[(F[_], α) => F[α]]] = null
  implicit def drosteSolveCofree[E]: Solve.Aux[Cofree[?[_], E], EnvT[E, ?[_], ?]] = null
  implicit def drosteSolveCatsCofree[E]: Solve.Aux[cats.free.Cofree[?[_], E], EnvT[E, ?[_], ?]] = null
}
