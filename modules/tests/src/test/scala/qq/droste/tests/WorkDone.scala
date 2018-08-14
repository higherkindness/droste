package qq.droste
package tests

import org.scalacheck.Properties
import org.scalacheck.Prop._

import data.list._
import data.{:<, Cofree}

final class WorkDone extends Properties("WorkDone") {

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
    forAll { list: List[Unit] =>
      val sf = new Sketch()
      val sg = new Sketch()

      val rf = scheme.cata[ListF[Unit, ?], List[Unit], Int](cataAlgebra(sf)).apply(list)
      val rg = scheme.gcata[ListF[Unit, ?], List[Unit], Int, Int](cataAlgebra(sg))(Gather.cata).apply(list)

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

    forAll { list: List[Unit] =>
      val sf = new Sketch()
      val sg = new Sketch()

      val rf = scheme.zoo.histo[ListF[Unit, ?], List[Unit], Int](histoAlgebra(sf)).apply(list)
      val rg = scheme.gcata[ListF[Unit, ?], List[Unit], Cofree[ListF[Unit, ?], Int], Int](histoAlgebra(sg))(Gather.histo).apply(list)

      ((rf ?= rg) :| "same result") &&
      ((sf.value ?= sg.value) :| "same work")
    }

  }

}
