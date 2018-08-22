package qq.droste.examples.pro

import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.~>

import qq.droste.{Algebra, scheme}
import qq.droste.data.list._

final class SmallPre extends Properties("SmallPre") {

  def filterNT(lim : Int): ListF[Int, ?] ~> ListF[Int, ?] =
    λ[ListF[Int, ?] ~> ListF[Int, ?]] {
      case NilF => NilF
      case t@ConsF(h, _) if h <= lim => t
      case ConsF(_, _) => NilF
    }

  val sumAlg = Algebra[ListF[Int, ?], Int] {
    case ConsF(h, t) => h + t
    case NilF => 0
  }

  val smallSum = scheme.zoo.prepro[ListF[Int, ?], List[Int], Int](filterNT(10), sumAlg)

  property("empty sum") =
    smallSum(Nil) ?= 0

  property("small sum") =
    smallSum(List(1, 2, 3)) ?= 6

  property("mixed sum") =
    smallSum((1 to 100).toList) ?= 55


}