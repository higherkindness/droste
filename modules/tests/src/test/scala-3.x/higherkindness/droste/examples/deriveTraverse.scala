package higherkindness.droste
package examples

import cats.Traverse
import cats.instances.option._
import cats.instances.list._
import cats.syntax.traverse._

import org.scalacheck.Properties
import org.scalacheck.Prop._

import higherkindness.droste.data._
import higherkindness.droste.macros.DeriveTraverse.deriveTraverse

sealed trait ExprDerivingTraverse[A]
object ExprDerivingTraverse {
  final case class Box[A](name: String, a: A)
  object Box {
    implicit val traverse: Traverse[Box] = deriveTraverse[Box]
  }
  final case class Dummy()

  final case class Const[A](value: BigDecimal) extends ExprDerivingTraverse[A]
  final case class Add[A](x: Box[A], y: A)     extends ExprDerivingTraverse[A]
  final case class AddList[A](list: List[Box[Option[A]]])
      extends ExprDerivingTraverse[A]

  implicit val traverse: Traverse[ExprDerivingTraverse] = deriveTraverse[ExprDerivingTraverse]
}

final class DeriveTraverseChecks extends Properties("deriveTraverse") {
  import ExprDerivingTraverse._

  val summingAlgebraM: AlgebraM[Option, ExprDerivingTraverse, BigDecimal] =
    AlgebraM {
      case Const(value)                           => Some(value)
      case Add(ExprDerivingTraverse.Box(_, x), y) => Some(x + y)
      case AddList(list)                          => list.map(_.a).sequence.map(_.reduce(_ + _))
    }

  val evaluate: Fix[ExprDerivingTraverse] => Option[BigDecimal] =
    scheme.cataM(summingAlgebraM)

  property("1") =
    evaluate(Fix(Const(1))) ?= Some(1)

  property("1 + 1") =
    evaluate(Fix(Add(Box("qwer", Fix(Const(1))), Fix(Const(1))))) ?= Some(2)

  property("1 + 2 + 5") =
    evaluate(
      Fix(
        Add(
          Box("qwer", Fix(Add(Box("qwer", Fix(Const(1))), Fix(Const(2))))),
          Fix(Const(5))))) ?= Some(8)

  property("1 + 2 + 3 + 4 + 5") =
    evaluate(
      Fix(AddList(List(
        Box("asdf", Option(Fix(Const(1)))),
        Box("asdf", Option(Fix(Const(2)))),
        Box("asdf", Option(Fix(Const(3)))),
        Box("asdf", Option(Fix(Const(4)))),
        Box("asdf", Option(Fix(Const(5))))
      )))) ?= Some(15)

}
