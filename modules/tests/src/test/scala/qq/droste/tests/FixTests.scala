package qq.droste
package tests

import data.Fix
import laws.BasisLaws

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Properties

import cats.instances.option._

final class FixTests extends Properties("Fix") {

  implicit val arbFixOption: Arbitrary[Fix[Option]] =
    Arbitrary(Gen.sized(maxSize =>
      scheme.anaM(CoalgebraM((size: Int) =>
        Gen.choose(0, size).map(n =>
          if (n > 0) Some(n) else None))).apply(maxSize)))

  include(BasisLaws.props[Option, Fix[Option]](
    "Option", "Fix[Option]"))

}
