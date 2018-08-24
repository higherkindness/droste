package qq.droste.examples.mathexpr

import org.scalacheck.Properties
import org.scalacheck.Prop._

import algebra.ring.Field
import algebra.instances.all._
import cats.implicits._

import qq.droste._
import qq.droste.data._
import qq.droste.data.prelude._
import qq.droste.syntax.alias._

// note:
// athema is a dummy math engine packaged with droste
// we're using it for these tests
import qq.athema._

final class MathExprExample extends Properties("MathExprExample") {

  def evalAlgebraMWithOverride[V: Field](
    variables: Map[String, V]
  ): AlgebraM[String | ?, AttrF[Option[V], Expr[V, ?], ?], V] = AlgebraM {
    val algebra = Evaluate.algebraM(variables)
    fa => fa match {
      case AttrF(Some(value), _) => value.asRight
      case AttrF(None,lower) => algebra(lower)
    }
  }

  property("cofree expressions") = {
    val f = scheme.cataM(evalAlgebraMWithOverride[Double](Map.empty))

    val p1 = f(Attr(None, Const(1.0))) ?= 1.0.asRight
    val p2 = f(Attr(Some(100.0), Const(1.0))) ?= 100.0.asRight
    val p3 = f(Attr(None, Add(Attr(None, Const(1.0)), Attr(None, Const(2.0))))) ?= 3.0.asRight
    val p4 = f(Attr(None, Add(Attr(None, Const(1.0)), Attr(Some(10.0), Const(2.0))))) ?= 11.0.asRight

    p1 && p2 && p3 && p4
  }

  property("mu expressions") = {

    val toMu = scheme.hylo(
      Mu.algebra[Expr[Double, ?]],
      Fix.coalgebra[Expr[Double, ?]])

    val f1: Mu[Expr[Double, ?]] = toMu(Fix(Const(1.0)))
    val f2: Mu[Expr[Double, ?]] = toMu(Fix(Add(Fix(Const(1.0)), Fix(Const(2.0)))))
    val f3: Mu[Expr[Double, ?]] = toMu(Fix(Neg(Fix(Add(Fix(Const(1.0)), Fix(Const(2.0)))))))

    val algebra = Algebra(Evaluate.algebraM[Double](Map.empty).run.andThen(_.fold(sys.error, identity)))

    val p1 = f1(algebra) ?= 1.0
    val p2 = f2(algebra) ?= 3.0
    val p3 = f3(algebra) ?= -3.0

    p1 && p2 && p3
  }

  property("nu expressions") = {

    val toNu = scheme.hylo(
      Nu.algebra[Expr[Double, ?]],
      Fix.coalgebra[Expr[Double, ?]])

    val fixed: Fix[Expr[Double, ?]] = Fix(Add(Fix(Const(10.0)), Fix(Neg(Fix(Add(Fix(Const(1.0)), Fix(Const(2.0))))))))
    val z: Nu[Expr[Double, ?]] = toNu(fixed)

    val a = z.a
    //println(a)
    val b = z.unfold(a)
    //println(b)
    val c = b.map(z.unfold.run)
    //println(c)
    val d = c.map(_.map(z.unfold.run))
    //println(d)
    val e = d.map(_.map(_.map(z.unfold.run)))
    //println(e)

    val p1 = (c: Any) != (d: Any)
    val p2 = (d: Any) ?= (e: Any)
    val p3 = (e: Any) ?= (fixed: Any)

    p1 && p2 && p3
  }

}
