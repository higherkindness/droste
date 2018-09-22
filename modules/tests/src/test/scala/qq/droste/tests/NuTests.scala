package qq.droste
package tests

import data.Nu
import laws.BasisLaws
import scalacheck._

import org.scalacheck.Properties

import cats.instances.option._

final class NuTests extends Properties("Nu") {

  include(BasisLaws.props[Option, Nu[Option]](
    "Option", "Nu[Option]"))

}
