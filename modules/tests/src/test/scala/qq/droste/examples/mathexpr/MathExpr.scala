package qq.droste.examples.mathexpr

import org.scalacheck.Properties
import org.scalacheck.Prop._

import algebra.ring.Field
import algebra.instances.all._
import cats.Applicative
import cats.Traverse
import cats.implicits._

import qq.droste._
import qq.droste.data._
import qq.droste.syntax._

sealed trait Expr[V, A]
final case class Var[V, A](name: String) extends Expr[V, A]
final case class Const[V, A](value: V) extends Expr[V, A]
final case class Neg[V, A](x: A) extends Expr[V, A]
final case class Add[V, A](x: A, y: A) extends Expr[V, A]
final case class Sub[V, A](x: A, y: A) extends Expr[V, A]
final case class Prod[V, A](x: A, y: A) extends Expr[V, A]
final case class Div[V, A](x: A, y: A) extends Expr[V, A]

object Expr {
  implicit def traverseExpr[V]: Traverse[Expr[V, ?]] = new DefaultTraverse[Expr[V, ?]] {

    def traverse[G[_]: Applicative, A, B](fa: Expr[V, A])(f: A => G[B]): G[Expr[V, B]] = fa match {
      case v: Var[_, B @unchecked]   => (v: Expr[V, B]).pure[G]
      case c: Const[_, B @unchecked] => (c: Expr[V, B]).pure[G]
      case e: Neg[V, A]              => f(e.x) map (Neg(_))
      case e: Add[V, A]              => (f(e.x), f(e.y)) mapN (Add(_, _))
      case e: Sub[V, A]              => (f(e.x), f(e.y)) mapN (Sub(_, _))
      case e: Prod[V, A]             => (f(e.x), f(e.y)) mapN (Prod(_, _))
      case e: Div[V, A]              => (f(e.x), f(e.y)) mapN (Div(_, _))
    }
  }
}

final class MathExprExample extends Properties("MathExprExample") {

  def evalAlgebraM[V](
    variables: Map[String, V]
  )(implicit V: Field[V]): AlgebraM[String | ?, Expr[V, ?], V] = {
    case v: Var[_, _] => variables.get(v.name).toRight(s"unknown variable: ${v.name}")
    case Const(v)     => v.asRight
    case Neg(x)       => V.negate(x).asRight
    case Add(x, y)    => V.plus(x, y).asRight
    case Sub(x, y)    => V.plus(x, V.negate(y)).asRight
    case Prod(x, y)   => V.times(x, y).asRight
    case Div(x, y)    => V.div(x, y).asRight
  }

  def evalAlgebraMWithOverride[V: Field](
    variables: Map[String, V]
  ): AlgebraM[String | ?, EnvT[Option[V], Expr[V, ?], ?], V] = {
    val algebra = evalAlgebraM(variables)
    fa => fa.ask match {
      case Some(value) => value.asRight
      case None        => algebra(fa.lower)
    }
  }

  def derivativeAlgebra[V](implicit V: Field[V]): RAlgebra[Fix[Expr[V, ?]], Expr[V, ?], Fix[Expr[V, ?]]] = {
    case Var(_)                 => Fix(Const(V.one))
    case Const(_)               => Fix(Const(V.zero))
    case Neg((_, xx))           => xx
    case Add((_, xx), (_, yy))  => Fix(Add(xx, yy))
    case Sub((_, xx), (_, yy))  => Fix(Sub(xx, yy))
    case Prod((x, xx), (y, yy)) => Fix(Add(Fix(Prod(x, yy)), Fix(Prod(xx, y))))
    case Div((x, xx), (y, yy))  => Fix(Div(Fix(Sub(Fix(Prod(xx, y)), Fix(Prod(x, yy)))), Fix(Prod(y, y))))
  }

  def simplifyAlgebra[V](implicit V: Field[V]): Algebra[Expr[V, ?], Fix[Expr[V, ?]]] = { fa =>
    val Zero: Fix[Expr[V, ?]] = Fix(Const(V.zero))
    val One: Fix[Expr[V, ?]] = Fix(Const(V.one))
    val Two: Fix[Expr[V, ?]] = Fix(Const(V.plus(V.one, V.one)))

    fa match {
      case Prod(Zero, _) => Zero
      case Prod(_, Zero) => Zero
      case Prod(One, v) => v
      case Prod(v, One) => v
      case Sub(Zero, v) => Fix(Neg(v))
      case Add(x, y) if x == y => Fix(Prod(Two, x))
      case other => Fix(other)
    }
  }

  property("derive!") = {
    val derive: Fix[Expr[Double, ?]] => Fix[Expr[Double, ?]] =
      scheme.para(derivativeAlgebra[Double])

    val simplify: Fix[Expr[Double, ?]] => Fix[Expr[Double, ?]] =
      scheme.cata(simplifyAlgebra[Double])

    val t1: Fix[Expr[Double, ?]] = Fix(Const(1.0))
    println("> " + simplify(derive(t1)))

    val t2: Fix[Expr[Double, ?]] = Fix(Prod(Fix(Var("x")), Fix(Var("x"))))
    println("> " + simplify(derive(t2)))

    val t3: Fix[Expr[Double, ?]] = Fix(Div(Fix(Const(5.0)), Fix(Var("x"))))
    println("  " + simplify(derive(t3)))

    true
  }

  property("fix expressions") = {
    val f = scheme.cataM(evalAlgebraM[Double](Map.empty))

    val p1 = f(Fix(Const(1.0))) ?= 1.0.asRight
    val p2 = f(Fix(Add(Fix(Const(1.0)), Fix(Const(2.0))))) ?= 3.0.asRight
    val p3 = f(Fix(Neg(Fix(Add(Fix(Const(1)), Fix(Const(2.0))))))) ?= -3.0.asRight

    p1 && p2 && p3
  }

  property("cofree expressions") = {
    val f = scheme.cataM(evalAlgebraMWithOverride[Double](Map.empty))

    val p1 = f(Cofree(None, Const(1.0))) ?= 1.0.asRight
    val p2 = f(Cofree(Some(100.0), Const(1.0))) ?= 100.0.asRight
    val p3 = f(Cofree(None, Add(Cofree(None, Const(1.0)), Cofree(None, Const(2.0))))) ?= 3.0.asRight
    val p4 = f(Cofree(None, Add(Cofree(None, Const(1.0)), Cofree(Some(10.0), Const(2.0))))) ?= 11.0.asRight

    p1 && p2 && p3 && p4
  }

  property("mu expressions") = {

    val toMu = scheme.hylo(
      Mu.algebra[Expr[Double, ?]],
      Fix.coalgebra[Expr[Double, ?]])

    val f1: Mu[Expr[Double, ?]] = toMu(Fix(Const(1.0)))
    val f2: Mu[Expr[Double, ?]] = toMu(Fix(Add(Fix(Const(1.0)), Fix(Const(2.0)))))
    val f3: Mu[Expr[Double, ?]] = toMu(Fix(Neg(Fix(Add(Fix(Const(1.0)), Fix(Const(2.0)))))))

    val algebra = evalAlgebraM[Double](Map.empty).andThen(_.fold(sys.error, identity))

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
    val c = b.map(z.unfold)
    //println(c)
    val d = c.map(_.map(z.unfold))
    //println(d)
    val e = d.map(_.map(_.map(z.unfold)))
    //println(e)

    val p1 = (c: Any) != (d: Any)
    val p2 = (d: Any) ?= (e: Any)
    val p3 = (e: Any) ?= (fixed: Any)

    p1 && p2 && p3
  }

}
