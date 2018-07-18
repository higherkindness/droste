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
    case ConsF(_, n :< _) =>
      sketch.value += 1
      n + 1
  }

  property("histo vs gcata") = {

    forAll { (list: Fix[ListF[Unit, ?]]) =>
      val sf = new Sketch()
      val sg = new Sketch()
      val sh = new Sketch()

      val f = scheme.histo(algebra(sf))
      val g = scheme.gcata_cranky(dist.cofree[ListF[Unit, ?]], algebra(sg))
      val h = scheme.gcata(gather.histo[ListF[Unit, ?], Int], algebra(sh))

      val rf = f(list)
      val rg = g(list)
      val rh = h(list)

      val p0 = (rf ?= rg) :| "histo result == gcata (old) result"
      val p1 = (rf ?= rh) :| "histo result == gcata (new) result"

      val p2 = if (sf.value <= 1) proved else (sf.value != sg.value) :| "histo work != gcata (old) work"
      val p3 = (sf.value ?= sh.value) :| "histo work == gcata (new) work"

      p0 && p1 && p2 && p3

    }

  }

}
