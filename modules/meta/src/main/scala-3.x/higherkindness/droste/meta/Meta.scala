package higherkindness.droste
package meta

import scala.quoted.*

object Meta {
  private def unroll(f: Expr[Any])(using q: Quotes): q.reflect.Term = {
    import quotes.reflect.*

    def recur(a: Term): Term =
      a match {
        case Typed(b, _) => recur(b)
        case TypeApply(Select(b, "asInstanceOf"), _) => recur(b)
        case Inlined(call, bindings, exp) => Inlined(call, bindings, recur(exp))
        case b => b
      }

    recur(f.asTerm)
  }

  private def impl[T: Type, U: Type](f: Expr[T])(using Quotes): Expr[U] = {
    import quotes.reflect.*

    if (TypeRepr.of[T] <:< TypeRepr.of[U]) {
      unroll(f).asExprOf[U]
    } else {
      val t = unroll(f).asExpr
      '{ $t.asInstanceOf[U] }
    }
  }

  inline def fastCast[T, U](inline f: T): U =
    ${ impl[T, U]('f) }
}
