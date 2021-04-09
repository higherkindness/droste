package higherkindness.droste

import data.{Attr, AttrF, CoattrF, Fix}

private[droste] trait FloatingBasisSolveInstances {
  import Basis.Solve

  implicit val drosteSolveFix: Solve.Aux[Fix, [F[_], X] =>> F[X]] = null
  implicit def drosteSolveAttr[A]: Solve.Aux[[F[_]] =>> Attr[F, A], [F[_], X] =>> AttrF[F, A, X]] =
    null
  implicit def drosteSolveCatsCofree[A]: Solve.Aux[
    [F[_]] =>> cats.free.Cofree[F, A],
    [F[_], X] =>> AttrF[F, A, X]] =
    null

  implicit def drosteSolveCatsFree[A]: Solve.Aux[
    [F[_]] =>> cats.free.Free[F, A],
    [F[_], X] =>> CoattrF[F, A, A]] =
    null
}
