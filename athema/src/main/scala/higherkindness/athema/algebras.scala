package higherkindness.athema

import algebra.ring.Field
import algebra.ring.Ring
import cats.implicits._

import higherkindness.droste._
import higherkindness.droste.syntax.all._

object Evaluate {
  def evaluate[V: Field]: Expr.Fixed[V] => Either[String, V] =
    scheme.cataM(algebraM(Map.empty))

  def algebraM[V](
      variables: Map[String, V]
  )(implicit V: Field[V]): AlgebraM[Either[String, *], Expr[V, *], V] =
    AlgebraM {
      case Var(name)  => variables.get(name).toRight(s"unknown variable: $name")
      case Const(v)   => v.asRight
      case Neg(x)     => V.negate(x).asRight
      case Add(x, y)  => V.plus(x, y).asRight
      case Sub(x, y)  => V.plus(x, V.negate(y)).asRight
      case Prod(x, y) => V.times(x, y).asRight
      case Div(x, y)  => V.div(x, y).asRight
    }
}

object Differentiate {

  def differentiate[V: Ring](wrt: String): Expr.Fixed[V] => Expr.Fixed[V] =
    scheme.gcata(algebra[V](wrt))(Gather.para)

  def algebra[V](wrt: String)(
      implicit V: Ring[V]): RAlgebra[Expr.Fixed[V], Expr[V, *], Expr.Fixed[V]] =
    RAlgebra {
      case Var(`wrt`)             => Const(V.one).fix
      case _: Var[_, _]           => Const(V.zero).fix
      case _: Const[_, _]         => Const(V.zero).fix
      case Neg((_, xx))           => Neg(xx)
      case Add((_, xx), (_, yy))  => Add(xx, yy)
      case Sub((_, xx), (_, yy))  => Sub(xx, yy)
      case Prod((x, xx), (y, yy)) => Add(Prod(x, yy), Prod(xx, y))
      case Div((x, xx), (y, yy)) =>
        Div(Sub(Prod(xx, y), Prod(x, yy)), Prod(y, y))
    }
}

object Simplify {
  def simplify[V: Field]: Expr.Fixed[V] => Expr.Fixed[V] =
    scheme.cata(algebra[V])

  def trans[V](
      implicit V: Field[V]): Trans[Expr[V, *], Expr[V, *], Expr.Fixed[V]] =
    Trans { fa =>
      val Zero = V.zero
      val One  = V.one
      val Two  = V.plus(V.one, V.one)

      fa match {

        case Prod(Const(Zero), _) => Const(Zero)
        case Prod(_, Const(Zero)) => Const(Zero)
        case Prod(Const(One), v)  => v.unfix
        case Prod(v, Const(One))  => v.unfix

        case Sub(Const(Zero), v) => Neg(v)

        case Add(Const(Zero), v) => v.unfix
        case Add(v, Const(Zero)) => v.unfix
        case Add(x, y) if x == y => Prod(Const(Two).fix, x)
        case Add(x, Prod(Const(n: V), y)) if x == y =>
          Prod(Const(V.plus(n, V.one)).fix, x)

        case other => other
      }
    }

  def algebra[V](implicit V: Field[V]): Algebra[Expr[V, *], Expr.Fixed[V]] =
    trans.algebra

}
