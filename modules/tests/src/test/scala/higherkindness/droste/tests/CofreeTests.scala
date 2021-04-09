package higherkindness.droste
package tests

import prelude._
import data.prelude._
import data.AttrF

import cats.free.Cofree
import cats.implicits._

import laws.BasisLaws
import scalacheck._

import org.scalacheck.Properties

final class CofreeTests extends Properties("Cofree") {

  include(
    BasisLaws.props[AttrF[Option, Int, *], Cofree[Option, Int]](
      "AttrF[Option, Int, *]",
      "Cofree[Option, Int]"))
}
