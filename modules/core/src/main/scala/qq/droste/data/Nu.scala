package qq.droste
package data

import cats.Functor
import cats.syntax.functor._

/** Nu is the greatest fixed point of a functor `F`. It is a
  * computation that can generate a coinductive infinite
  * structure on demand.
  *
  * In Haskell this can more aptly be expressed as:
  * `data Nu g = forall s . Nu (s -> g s) s`
  */
sealed abstract class Nu[F[_]] extends Serializable {
  type A = F[Nu[F]]
  def  unfold: Coalgebra[F, A]
  def  a     : A

  override final def toString: String = s"Nu($unfold, $a)"
}

object Nu {
  def apply[F[_]](unfold0: Coalgebra[F, F[Nu[F]]], a0: F[Nu[F]]): Nu[F] =
    Default(unfold0, a0)

  def algebra[F[_]: Functor]: Algebra[F, Nu[F]] =
    t => MuEqA((_: F[Nu[F]]) map coalgebra, t)

  def coalgebra[F[_]: Functor]: Coalgebra[F, Nu[F]] =
    nf => nf.unfold(nf.a) map (MuEqA(nf.unfold, _))

  def embed  [F[_]: Functor](fnf: F[Nu[F]]):   Nu[F]  = algebra  [F].apply(fnf)
  def project[F[_]: Functor](nf :   Nu[F] ): F[Nu[F]] = coalgebra[F].apply(nf)

  private final case class Default[F[_]](unfold: Coalgebra[F, F[Nu[F]]], a: F[Nu[F]]) extends Nu[F]

  // Arranged so that equality is done only over the value `a`. This
  // should only be used by the algebra/coalgebra methods above.
  private final case class MuEqA[F[_]](a: F[Nu[F]])(unfold0: Coalgebra[F, F[Nu[F]]]) extends Nu[F] {
    val unfold = unfold0
  }

  private object MuEqA {
    def apply[F[_]](unfold: Coalgebra[F, F[Nu[F]]], a: F[Nu[F]]): Nu[F] = MuEqA(a)(unfold)
  }

  implicit def drosteBasisForNu[F[_]: Functor]: Basis[F, Nu[F]] =
    Basis.Default[F, Nu[F]](Nu.algebra, Nu.coalgebra)

  implicit val drosteBasisSolveForNu: Basis.Solve.Aux[Nu, λ[(F[_], α) => F[α]]] = null
}
