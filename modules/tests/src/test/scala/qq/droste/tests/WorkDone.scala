package qq.droste
package test

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Properties
import org.scalacheck.Prop._

import data.prelude._
import data.list._
import data.Fix
import data.:<

final class WorkDone extends Properties("WorkDone") {

  implicit def aribtraryFixListF[A: Arbitrary]: Arbitrary[Fix[ListF[A, ?]]] =
    Arbitrary(arbitrary[List[A]].map(_.take(10)).map(ListF.fromScalaList[A, Fix](_)))

  final class Sketch(var value: Int = 0)

  def algebra(sketch: Sketch): CVAlgebra[ListF[Unit, ?], Int] = {
    case NilF =>
      sketch.value += 1
      0
    case ConsF(_, n :< _) => n + 1
  }

  property("histo vs gcata") = {

    forAll { (list: Fix[ListF[Unit, ?]]) =>
      val sf = new Sketch()
      val sg = new Sketch()

      val f = scheme.histo(algebra(sf))
      val g = scheme.gcata(dist.cofree[ListF[Unit, ?]], algebra(sg))

      (f(list) ?= g(list)) && (sg.value ?= sf.value)
    }

  }

}
