package higherkindness.athema

import cats.Functor
import higherkindness.droste._
import higherkindness.droste.data.Fix
import scala.annotation.nowarn

object `package` {

  implicit val functorExprFixed: Functor[Expr.Fixed] = new Functor[Expr.Fixed] {
    def map[A, B](fa: Expr.Fixed[A])(f: A => B): Expr.Fixed[B] =
      scheme.cata(mapAlgebra(f)).apply(fa)
  }

  @nowarn("msg=match may not be exhaustive")
  private def mapAlgebra[A, B](f: A => B): Algebra[Expr[A, *], Expr.Fixed[B]] =
    Algebra {
      case Const(c)                        => Fix(Const(f(c)))
      case other: Expr.Fixed[B] @unchecked => other
    }

}
