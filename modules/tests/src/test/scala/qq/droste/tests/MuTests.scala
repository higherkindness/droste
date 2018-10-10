package qq.droste
package tests

import data.Mu
import laws.BasisLaws
import scalacheck._

import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.instances.option._

final class MuTests extends Properties("Mu") {

  include(BasisLaws.props[Option, Mu[Option]](
    "Option", "Mu[Option]"))

  property("unapply") = {
    forAll((x: Mu[Option]) =>
      x match {
        case Mu(y) => y ?= Mu.un(x)
      }
    )
  }
}
