package droste
package scheme

import alias._
import data.Fix
import data.list._

import org.scalacheck.Properties
import org.scalacheck.Prop._

class AnaTests extends Properties("scheme.ana") {

  val fold: Algebra[ListF[String, ?], String] = {
    case ConsF(head, tail) => s"$head$tail"
    case NilF              => ""
  }

  val unfold: Coalgebra[ListF[String, ?], String] =
    input =>
      if (input.isEmpty) NilF
      else ConsF(input.take(1), input.drop(1))

  property("hylo <-> Fix") = {
    val f = hylo(fold, unfold)

    forAll((input: String) =>
      f(input) ?= input)
  }

  property("ana  --> Fix") = {
    val expected: String => Fix[ListF[String, ?]] =
      _.grouped(1).foldRight(Fix.fix[ListF[String, ?]](NilF))((c, acc) =>
        Fix.fix(ConsF(c, acc)))

    val f = ana(unfold)

    forAll((input: String) =>
      f(input) ?= expected(input))
  }

  property("cata <-- Fix") = {
    val unexpected: String => Fix[ListF[String, ?]] =
      _.grouped(1).foldRight(Fix.fix[ListF[String, ?]](NilF))((c, acc) =>
        Fix.fix(ConsF(c, acc)))

    val f = cata(fold)

    forAll((input: String) =>
      f(unexpected(input)) ?= input)
  }

}
