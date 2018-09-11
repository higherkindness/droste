package qq.droste
package tests

import data.Attr
import data.AttrF
import data.prelude._
import laws.BasisLaws

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Properties

import cats.implicits._

final class AttrTests extends Properties("Attr") {

  implicit def arbAttrOption[A: Arbitrary]: Arbitrary[Attr[Option, A]] =
    Arbitrary(Gen.sized(maxSize =>
      scheme.anaM(CoalgebraM((size: Int) =>
        (
          arbitrary[A],
          Gen.choose(0, size).map(n => if (n > 0) Some(n) else None)
        ) mapN (AttrF(_, _))
      )).apply(maxSize)))

  include(BasisLaws.props[AttrF[Option, Int, ?], Attr[Option, Int]](
    "AttrF[Int, Option, ?]", "Attr[Option, Int]"))

}
