package qq.droste
package data
package list

import cats.Applicative
import cats.Traverse
import cats.syntax.applicative._
import cats.syntax.functor._

import util.DefaultTraverse

sealed trait ListF[+A, +B]
final case class ConsF[A, B](head: A, tail: B) extends ListF[A, B]
case object NilF extends ListF[Nothing, Nothing]

object ListF {
  implicit def drosteTraverseForListF[A]: Traverse[ListF[A, ?]] =
    new DefaultTraverse[ListF[A, ?]] {
      def traverse[F[_]: Applicative, B, C](fb: ListF[A, B])(f: B => F[C]): F[ListF[A, C]] =
        fb match {
          case ConsF(head, tail) => f(tail).map(ConsF(head, _))
          case NilF              => (NilF: ListF[A, C]).pure[F]
        }
    }
}
