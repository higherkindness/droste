package qq.droste
package tests

import prelude._
import data.Fix
import laws.BasisLaws
import scalacheck._

import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.instances.option._

final class FixTests extends Properties("Fix") {

  include(BasisLaws.props[Option, Fix[Option]]("Option", "Fix[Option]"))

  property("unapply") = {
    forAll((x: Fix[Option]) =>
      x match {
        case Fix(y) => y ?= Fix.un(x)
    })
  }
}
