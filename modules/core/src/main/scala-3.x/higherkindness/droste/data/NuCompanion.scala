package higherkindness.droste.data

import higherkindness.droste.Basis

trait NuCompanion {
  implicit val drosteBasisSolveForNu: Basis.Solve.Aux[
    Nu,
    [F[_], α] =>> F[α]] = null
}
