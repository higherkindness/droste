package higherkindness.droste
package tests

import cats.kernel.laws.discipline.MonoidTests
import cats.Eq
import higherkindness.droste.util.newtypes._
import org.scalacheck._

final class NewtypesTests extends Properties("newtypes") {

  implicit val conjunctionArbitrary: Arbitrary[Boolean @@ Tags.Conjunction] =
    Arbitrary(Gen.oneOf(true, false).map(_.conjunction))

  implicit val disjunctionArbitrary: Arbitrary[Boolean @@ Tags.Disjunction] =
    Arbitrary(Gen.oneOf(true, false).map(_.disjunction))

  implicit val conjunctionEq: Eq[Boolean @@ Tags.Conjunction] =
    Eq.by(_.unwrap)

  implicit val disjunctionEq: Eq[Boolean @@ Tags.Disjunction] =
    Eq.by(_.unwrap)

  include(MonoidTests[Boolean @@ Tags.Conjunction].monoid.all, "conjunction.")
  include(MonoidTests[Boolean @@ Tags.Disjunction].monoid.all, "disjunction.")
}
