package qq.droste
package examples

import cats.instances.option._

import org.scalacheck.Properties
import org.scalacheck.Prop._

import qq.droste.data._
import qq.droste.macros.deriveTraverse

@deriveTraverse sealed trait ExprDerivingTraverse[A]
object ExprDerivingTraverse {
  final case class Const[A](value: BigDecimal) extends ExprDerivingTraverse[A]
  final case class Add[A](x: A, y: A) extends ExprDerivingTraverse[A]
  final case class AddList[A](list: List[A]) extends ExprDerivingTraverse[A]
}

final class DeriveTraverseChecks extends Properties("deriveTraverse") {
  import ExprDerivingTraverse._

  val summingAlgebraM: AlgebraM[Option, ExprDerivingTraverse, BigDecimal] = AlgebraM {
    case Const(value) => Some(value)
    case Add(x, y) => Some(x + y)
    case AddList(list) => Some(list.reduce(_ + _))
  }

  val evaluate: Fix[ExprDerivingTraverse] => Option[BigDecimal] = scheme.cataM(summingAlgebraM)

  property("1") =
    evaluate(Fix(Const(1))) ?= Some(1)

  property("1 + 1") =
    evaluate(Fix(Add(Fix(Const(1)), Fix(Const(1))))) ?= Some(2)

  property("1 + 2 + 5") =
    evaluate(Fix(Add(Fix(Add(Fix(Const(1)), Fix(Const(2)))), Fix(Const(5))))) ?= Some(8)

  property("1 + 2 + 3 + 4 + 5") =
    evaluate(Fix(AddList(List(Fix(Const(1)), Fix(Const(2)), Fix(Const(3)), Fix(Const(4)), Fix(Const(5)))))) ?= Some(15)
  

}
