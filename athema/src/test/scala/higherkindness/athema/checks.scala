package higherkindness.athema

import cats.syntax.all._
import algebra.instances.all._

import org.scalacheck.Properties
import org.scalacheck.Prop._

import higherkindness.droste.scheme
import higherkindness.droste.Gather

class Checks extends Properties("athema") {

  val fe  = Evaluate.evaluate[Double]
  val fd  = Differentiate.differentiate[Double]("x")
  val fs  = Simplify.simplify[Double]
  val fds = fd andThen fs

  lazy val fePairs: List[(Expr.Fixed[Double], Option[Double])] = List(
    Const.fix(1.0)                      -> 1.0.some,
    Add(Const.fix(1.0), Const.fix(2.0)) -> 3.0.some,
    Div(
      Sub(Prod(Const.fix(2.0), Const.fix(3.3)), Const.fix(0.6)),
      Const.fix(2.0)) -> 3.0.some
  )

  fePairs.foreach {
    case (in, out) =>
      property(s"evaluate $in") = {
        fe(in).toOption ?= out
      }
  }

  lazy val varx = Var.fix[Double]("x")
  lazy val vary = Var.fix[Double]("y")
  lazy val varz = Var.fix[Double]("z")

  lazy val fdPairs: List[(Expr.Fixed[Double], Expr.Fixed[Double])] = List(
    Const.fix(1.0) -> Const.fix(0.0),
    Prod(varx, varx) -> Add(
      Prod(varx, Const.fix(1.0)),
      Prod(Const.fix(1.0), varx)),
    Prod(vary, vary) -> Add(
      Prod(vary, Const.fix(0.0)),
      Prod(Const.fix(0.0), vary))
  )

  fdPairs.foreach {
    case (in, out) =>
      property(s"differentiate $in") = {
        fd(in) ?= out
      }
  }

  lazy val fdsPairs: List[(Expr.Fixed[Double], Expr.Fixed[Double])] = List(
    Const.fix(1.0)   -> Const.fix(0.0),
    Prod(varx, varx) -> Prod(Const.fix(2.0), varx),
    Prod(vary, vary) -> Const.fix(0.0)
  )

  fdsPairs.foreach {
    case (in, out) =>
      property(s"differentiate and simplify $in") = {
        fds(in) ?= out
      }
  }

  // let's compute the gradient (three partial derivatives)
  // in one pass over the original structure

  property("∇f") = {

    type Ex = Expr.Fixed[Double]
    final case class IJK(i: Ex, j: Ex, k: Ex)

    lazy val exprs: List[(Ex, IJK)] = List(
      Prod(varx, varx) -> IJK(
        Prod(Const.fix(2.0), varx),
        Const.fix(0.0),
        Const.fix(0.0)),
      Prod(varx, vary) -> IJK(vary, varx, Const.fix(0.0)),
      Prod(vary, vary) -> IJK(
        Const.fix(0.0),
        Prod(Const.fix(2.0), vary),
        Const.fix(0.0)),
      Prod(Prod(varx, vary), varz) -> IJK(
        Prod(vary, varz),
        Prod(varx, varz),
        Prod(varx, vary))
    )

    val f = scheme.gcata(
      Differentiate.algebra[Double]("x").gather(Gather.para) zip
        Differentiate.algebra[Double]("y").gather(Gather.para) zip
        Differentiate.algebra[Double]("z").gather(Gather.para))

    val g = scheme.gcata(Simplify.algebra[Double])(Gather.cata)
    val h = f andThen { case ((i, j), k) => IJK(g(i), g(j), g(k)) }

    exprs
      .map {
        case (in, expected) =>
          h(in) ?= expected
      }
      .reduce(_ && _)
  }

}
