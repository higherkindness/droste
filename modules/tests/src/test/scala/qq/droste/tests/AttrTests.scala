package qq.droste
package tests

import data.Attr
import data.AttrF
import data.prelude._
import laws.BasisLaws
import scalacheck._

import org.scalacheck.Properties

import cats.implicits._
import cats.laws.discipline.TraverseTests

final class AttrTests extends Properties("Attr/AttrF") {

  include(BasisLaws.props[AttrF[Option, Int, ?], Attr[Option, Int]](
    "AttrF[Int, Option, ?]", "Attr[Option, Int]"))

  include(TraverseTests[AttrF[Option, Int, ?]].traverse[Int, Int, Int, Int, Option, Option].all)

}
