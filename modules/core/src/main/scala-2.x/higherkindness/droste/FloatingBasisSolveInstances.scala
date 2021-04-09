package higherkindness.droste

import data.{Attr, AttrF, CoattrF, Fix}

private[droste] trait FloatingBasisSolveInstances {
  import Basis.Solve

  implicit val drosteSolveFix: Solve.Aux[Fix, λ[(F[_], α) => F[α]]] = null
  implicit def drosteSolveAttr[A]: Solve.Aux[Attr[*[_], A], AttrF[*[_], A, *]] =
    null
  implicit def drosteSolveCatsCofree[A]: Solve.Aux[
    cats.free.Cofree[*[_], A],
    AttrF[*[_], A, *]] =
    null

  implicit def drosteSolveCatsFree[A]: Solve.Aux[
    cats.free.Free[*[_], A],
    CoattrF[*[_], A, *]] =
    null
}
