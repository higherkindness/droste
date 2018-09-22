package qq.droste
package tests

import data.Mu
import laws.BasisLaws
import scalacheck._

import org.scalacheck.Properties

import cats.instances.option._

final class MuTests extends Properties("Mu") {

  include(BasisLaws.props[Option, Mu[Option]](
    "Option", "Mu[Option]"))

}
