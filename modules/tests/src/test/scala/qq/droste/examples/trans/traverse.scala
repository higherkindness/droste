package qq.droste
package examples.trans

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Properties
import org.scalacheck.Cogen



// import cats._
// import cats.Eq
import cats.implicits._
import cats.laws.discipline.TraverseTests
// import cats.laws.discipline.eq._

final class TransTraverseTests extends Properties("TransTraverseTests") {
  import TransDemo._

  implicit def neListFArb[A : Arbitrary, Z: Arbitrary]: Arbitrary[NeListF[A,Z]] = Arbitrary(for {
    a <- arbitrary[A]
    z <- arbitrary[Z]
    l <- Gen.oneOf(NeLastF[A,Z](a), NeConsF(a,z))
  } yield l)
 
  include(TraverseTests[NeListF[String, ?]]
    .traverse[Int, Int, Int, Int, Option, Option].all, "TraverseTests[NeListF[String, ?]]")
}

