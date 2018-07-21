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

  def toScalaList[A, PatR[_[_]]](list: PatR[ListF[A, ?]])(
    implicit ev: Project[ListF[A, ?], PatR[ListF[A, ?]]]
  ): List[A] =
    scheme.cata(toScalaListAlgebra[A]).apply(list)

  def toScalaListAlgebra[A]: Algebra[ListF[A, ?], List[A]] = Algebra {
    case ConsF(head, tail) => head :: tail
    case NilF              => Nil
  }

  def fromScalaList[A, PatR[_[_]]](list: List[A])(
    implicit ev: Embed[ListF[A, ?], PatR[ListF[A, ?]]]
  ): PatR[ListF[A, ?]] =
    scheme.ana(fromScalaListCoalgebra[A]).apply(list)

  def fromScalaListCoalgebra[A]: Coalgebra[ListF[A, ?], List[A]] = Coalgebra {
    case head :: tail => ConsF(head, tail)
    case Nil          => NilF
  }

  implicit def drosteTraverseForListF[A]: Traverse[ListF[A, ?]] =
    new DefaultTraverse[ListF[A, ?]] {
      def traverse[F[_]: Applicative, B, C](fb: ListF[A, B])(f: B => F[C]): F[ListF[A, C]] =
        fb match {
          case ConsF(head, tail) => f(tail).map(ConsF(head, _))
          case NilF              => (NilF: ListF[A, C]).pure[F]
        }
    }
}
