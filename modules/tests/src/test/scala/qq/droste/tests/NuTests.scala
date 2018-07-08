package qq.droste
package tests

import data.Nu
import laws.BasisLaws

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Properties

import cats.instances.option._

final class NuTests extends Properties("Nu") {

  implicit val arbNuOption: Arbitrary[Nu[Option]] =
    Arbitrary(Gen.sized(maxSize =>
      scheme[Nu].anaM((size: Int) =>
        Gen.choose(0, size).flatMap(n =>
          if (n > 0) Some(n) else None)).apply(maxSize)))

  include(BasisLaws.props[Option, Nu[Option]](
    "Option", "Nu[Option]"))

}
