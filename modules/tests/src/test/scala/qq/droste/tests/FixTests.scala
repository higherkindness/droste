package qq.droste
package tests

import data.Fix
import laws.BasisLaws
import scalacheck._

import org.scalacheck.Properties

import cats.instances.option._

final class FixTests extends Properties("Fix") {

  include(BasisLaws.props[Option, Fix[Option]](
    "Option", "Fix[Option]"))

}
