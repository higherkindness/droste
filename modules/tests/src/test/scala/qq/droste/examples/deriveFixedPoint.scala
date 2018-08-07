package qq.droste
package examples

import org.scalacheck.Properties
import org.scalacheck.Prop._

import qq.droste.macros.deriveFixedPoint

@deriveFixedPoint sealed trait RecursiveExpr
object RecursiveExpr {
  final case class Const(value: BigDecimal) extends RecursiveExpr
  final case class Add(x: RecursiveExpr, y: RecursiveExpr) extends RecursiveExpr
}

// This example is basically the same as expr2.scala but with the
// boilerplate removed by @deriveFixedPoint.
final class RecursiveExprChecks extends Properties("deriveFixedPoint") {
  import RecursiveExpr._
  import RecursiveExpr.fixedpoint._

  val evaluateAlgebra: Algebra[RecursiveExprF, BigDecimal] = Algebra {
    case ConstF(v) => v
    case AddF(x, y) => x + y
  }

  val evaluate: RecursiveExpr => BigDecimal = scheme.cata(evaluateAlgebra)

  property("1") =
    evaluate(Const(1)) ?= 1

  property("1 + 1") =
    evaluate(Add(Const(1), Const(1))) ?= 2

  property("1 + 2 + 5") =
    evaluate(Add(Add(Const(1), Const(2)), Const(5))) ?= 8
}
