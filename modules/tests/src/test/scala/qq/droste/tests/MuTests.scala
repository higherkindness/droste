package qq.droste
package tests

import data.Mu
import laws.BasisLaws

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Properties

import cats.instances.option._

final class MuTests extends Properties("Mu") {

  implicit val arbMuOption: Arbitrary[Mu[Option]] =
    Arbitrary(Gen.sized(maxSize =>
      scheme[Mu].anaM(CoalgebraM((size: Int) =>
        Gen.choose(0, size).flatMap(n =>
          if (n > 0) Some(n) else None))).apply(maxSize)))

  include(BasisLaws.props[Option, Mu[Option]](
    "Option", "Mu[Option]"))

}
