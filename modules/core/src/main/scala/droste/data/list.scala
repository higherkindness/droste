package droste
package data
package list

import cats.Functor

import alias.Algebra
import alias.Coalgebra

sealed trait ListF[+A, +B]
final case class ConsF[A, B](head: A, tail: B) extends ListF[A, B]
final case object NilF extends ListF[Nothing, Nothing]

object ListF {

  def toList[A]: Algebra[ListF[A, ?], List[A]] = {
    case ConsF(head, tail) => head :: tail
    case NilF              => Nil
  }

  def fromList[A]: Coalgebra[ListF[A, ?], List[A]] = {
    case ::(head, tail) => ConsF(head, tail)
    case Nil            => NilF
  }

  implicit def functorInstance[A]: Functor[ListF[A, ?]] =
    new Functor[ListF[A, ?]] {
      def map[B, C](fb: ListF[A, B])(f: B => C): ListF[A, C] =
        fb match {
          case ConsF(head, tail) => ConsF(head, f(tail))
          case NilF              => NilF
        }
    }
}
