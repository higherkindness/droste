package qq.droste.examples.mathexpr

import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.Functor
import cats.Group
import cats.syntax.functor._
import cats.syntax.group._
import cats.instances.int._

import qq.droste._
import qq.droste.data._

sealed trait Expr[V, A]
final case class Const[V, A](value: V) extends Expr[V, A]
final case class Neg[V, A](x: A) extends Expr[V, A]
final case class Add[V, A](x: A, y: A) extends Expr[V, A]

object Expr {
  implicit def functorExpr[V]: Functor[Expr[V, ?]] = new Functor[Expr[V, ?]] {
    def map[A, B](fa: Expr[V, A])(f: A => B): Expr[V, B] = fa match {
      case c: Const[_, B @unchecked] => c
      case e: Neg[V, A] => e.copy(x = f(e.x))
      case e: Add[V, A] => Add(f(e.x), f(e.y))
    }
  }
}

final class MathExprExample extends Properties("MathExprExample") {

  def groupAlgebra[V: Group]: Algebra[Expr[V, ?], V] = {
    case Const(v) => v
    case Neg(x) => x.inverse
    case Add(x, y) => x combine y
  }

  def groupAnnotatedAlgebra[V: Group]: Algebra[EnvT[Option[V], Expr[V, ?], ?], V] = fa => fa.lower match {
    case Const(v) => fa.ask getOrElse v
    case Neg(x) => fa.ask getOrElse x.inverse
    case Add(x, y) => fa.ask getOrElse (x combine y)
  }

  property("fix expressions") = {
    val f = scheme.hylo(groupAlgebra[Int], Fix.coalgebra[Expr[Int, ?]])

    val p1 = f(Fix(Const(1))) ?= 1
    val p2 = f(Fix(Add(Fix(Const(1)), Fix(Const(2))))) ?= 3
    val p3 = f(Fix(Neg(Fix(Add(Fix(Const(1)), Fix(Const(2))))))) ?= -3

    p1 && p2 && p3
  }

  property("cofree expressions") = {
    val f = scheme.hylo(groupAnnotatedAlgebra[Int],
      Cofree.coalgebra[Option[Int], Expr[Int, ?]])

    val p1 = f(Cofree(None, Const(1))) ?= 1
    val p2 = f(Cofree(Some(100), Const(1))) ?= 100
    val p3 = f(Cofree(None, Add(Cofree(None, Const(1)), Cofree(None, Const(2))))) ?= 3
    val p4 = f(Cofree(None, Add(Cofree(None, Const(1)), Cofree(Some(10), Const(2))))) ?= 11

    p1 && p2 && p3 && p4
  }

  property("mu expressions") = {

    val toMu = scheme.hylo(
      Mu.algebra[Expr[Int, ?]],
      Fix.coalgebra[Expr[Int, ?]])

    val algebra = groupAlgebra[Int]

    val f1: Mu[Expr[Int, ?]] = toMu(Fix(Const(1)))
    val f2: Mu[Expr[Int, ?]] = toMu(Fix(Add(Fix(Const(1)), Fix(Const(2)))))
    val f3: Mu[Expr[Int, ?]] = toMu(Fix(Neg(Fix(Add(Fix(Const(1)), Fix(Const(2)))))))

    val p1 = f1(algebra) ?= 1
    val p2 = f2(algebra) ?= 3
    val p3 = f3(algebra) ?= -3

    p1 && p2 && p3
  }

  property("nu expressions") = {

    val toNu = scheme.hylo(
      Nu.algebra[Expr[Int, ?]],
      Fix.coalgebra[Expr[Int, ?]])

    val algebra = groupAlgebra[Int]

    val fixed: Fix[Expr[Int, ?]] = Fix(Add(Fix(Const(10)), Fix(Neg(Fix(Add(Fix(Const(1)), Fix(Const(2))))))))
    val z: Nu[Expr[Int, ?]] = toNu(fixed)

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
