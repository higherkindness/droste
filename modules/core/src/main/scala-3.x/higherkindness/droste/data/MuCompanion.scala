package higherkindness.droste.data

import higherkindness.droste.Basis

trait MuCompanion {
  implicit val drosteBasisSolveForMu: Basis.Solve.Aux[
    Mu,
    [F[_], α] =>> F[α]] = null
}
