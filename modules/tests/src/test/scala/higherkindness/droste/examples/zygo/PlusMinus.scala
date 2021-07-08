package higherkindness.droste.examples.zygo

import org.scalacheck.Properties
import org.scalacheck.Prop._

import higherkindness.droste.Algebra
import higherkindness.droste.RAlgebra
import higherkindness.droste.scheme
import higherkindness.droste.data.list._

final class PlusMinus extends Properties("PlusMinus") {

  val evenAlgebra = Algebra[ListF[Int, *], Boolean] {
    case NilF           => false
    case ConsF(_, bool) => !bool
  }

  val calcRAlgebra = RAlgebra[Boolean, ListF[Int, *], Int] {
    case NilF             => 0
    case ConsF(n, (b, x)) => if (b) n + x else n - x
  }

  // plusMinus(List(a,b,c,d,e)) = a - (b + (c - (d + e)))
  val plusMinus = scheme.zoo.zygo[ListF[Int, *], List[Int], Boolean, Int](
    evenAlgebra,
    calcRAlgebra)

  property("plus-minus of empty list") =
    plusMinus(List()) ?= 0

  property("plus-minus of increasing list") =
    plusMinus(List(1, 2, 3, 4, 5)) ?= 5

  property("plus-minus of alternating list") =
    plusMinus(List(1, -1, 1, -1, 1, -1)) ?= 0

}
