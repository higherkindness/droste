package qq.droste
package laws

import org.scalacheck.Arbitrary
import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop._

trait BasisLaws[F[_], R] {
  def basis: Basis[F, R]
  def algebraComposeCoalgebra(r: R): Prop =
      basis.algebra(basis.coalgebra(r)) ?= r
}

object BasisLaws {

  def props[F[_], R](nameF: String, nameR: String)(
    implicit
      ev: Basis[F, R],
      arbR: Arbitrary[R]
  ): Props[F, R] =
    new Props[F, R](s"Basis[$nameF, $nameR]")

  class Props[F[_], R](name: String)(
    implicit
      ev: Basis[F, R],
      arbR: Arbitrary[R]

  ) extends Properties(name) {
    val laws: BasisLaws[F, R] = new BasisLaws[F, R] {
      val basis = ev
    }

    property("algebra compose coalgebra") =
      forAll((r: R) => laws.algebraComposeCoalgebra(r))
  }

}
