package higherkindness.droste
package laws

import prelude._

import cats.Eq
import cats.syntax.eq._

import org.scalacheck.Arbitrary
import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop._

trait BasisLaws[F[_], R] {
  def basis: Basis[F, R]
  def algebraComposeCoalgebraIdentity(r: R)(implicit ev: Eq[R]): Prop =
    basis.algebra(basis.coalgebra(r)) === r
}

object BasisLaws {

  def props[F[_], R](nameF: String, nameR: String)(
      implicit
      ev: Basis[F, R],
      arbR: Arbitrary[R],
      deq: Delay[Eq, F]
  ): Props[F, R] =
    new Props[F, R](s"Basis[$nameF, $nameR]")

  class Props[F[_], R](name: String)(
      implicit
      ev: Basis[F, R],
      arbR: Arbitrary[R],
      deq: Delay[Eq, F]
  ) extends Properties(name) {
    val laws: BasisLaws[F, R] = new BasisLaws[F, R] {
      val basis = ev
    }

    property("algebra compose coalgebra identity") = forAll(
      (r: R) => laws.algebraComposeCoalgebraIdentity(r))
  }

}
