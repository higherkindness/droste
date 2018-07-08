package qq.droste
package tests

import data.Cofree
import data.EnvT
import data.prelude._
import laws.BasisLaws

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Properties

import cats.implicits._

final class CofreeTests extends Properties("Cofree") {

  implicit def arbCofreeOption[A: Arbitrary]: Arbitrary[Cofree[Option, A]] =
    Arbitrary(Gen.sized(maxSize =>
      scheme.anaM((size: Int) =>
        (
          arbitrary[A],
          Gen.choose(0, size).flatMap(n => if (n > 0) Some(n) else None)
        ) mapN (EnvT(_, _))
      ).apply(maxSize)))

  include(BasisLaws.props[EnvT[Int, Option, ?], Cofree[Option, Int]](
    "EnvT[Int, Option, ?]", "Cofree[Option, Int]"))

}
