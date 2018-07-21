package qq.droste
package tests

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Properties
import org.scalacheck.Prop._

import data.list._
import data.Fix
import data.:<

final class WorkDone extends Properties("WorkDone") {

  implicit def aribtraryFixListF[A: Arbitrary]: Arbitrary[Fix[ListF[A, ?]]] =
    Arbitrary(arbitrary[List[A]].map(_.take(10)).map(ListF.fromScalaList[A, Fix](_)))

  final class Sketch(var value: Int = 0)

  def cataAlgebra(sketch: Sketch): Algebra[ListF[Unit, ?], Int] = Algebra {
    case NilF =>
      sketch.value += 1
      0
    case ConsF(_, n) =>
      sketch.value += 1
      n + 1
  }

  property("cata vs gcata") = {
    forAll { (list: Fix[ListF[Unit, ?]]) =>
      val sf = new Sketch()
      val sg = new Sketch()

      val rf = scheme.cata(cataAlgebra(sf)).apply(list)
      val rg = scheme.gcata(cataAlgebra(sg))(Gather.cata).apply(list)

      ((rf ?= rg) :| "same result") &&
      ((sf.value ?= sg.value) :| "same work")
    }
  }

  def histoAlgebra(sketch: Sketch): CVAlgebra[ListF[Unit, ?], Int] = CVAlgebra {
    case NilF =>
      sketch.value += 1
      0
    case ConsF(_, n :< _) =>
      sketch.value += 1
      n + 1
  }

  property("histo vs gcata") = {

    forAll { (list: Fix[ListF[Unit, ?]]) =>
      val sf = new Sketch()
      val sg = new Sketch()

      val rf = scheme.zoo.histo(histoAlgebra(sf)).apply(list)
      val rg = scheme.gcata(histoAlgebra(sg))(Gather.histo).apply(list)

      ((rf ?= rg) :| "same result") &&
      ((sf.value ?= sg.value) :| "same work")
    }

  }

}
