package qq.droste
package tests

import data.CoattrF
import scalacheck._

import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.implicits._

final class CoattrFTests extends Properties("CoattrF") {

  property("unapply") = {
    forAll((x: CoattrF[Option, Int, Long]) =>
      x match {
        case CoattrF.Pure(i) => CoattrF.un(x).left.toOption ?= Some(i)
        case CoattrF.Roll(fi) => CoattrF.un(x).toOption ?= Some(fi)
      }
    )
  }
}
