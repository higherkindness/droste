package higherkindness.droste
package examples.trans

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.Applicative
import cats.Traverse
import cats.data.NonEmptyList
import cats.syntax.all._
import cats.instances.option._

import higherkindness.droste.data.Fix
import higherkindness.droste.data.list._
import higherkindness.droste.util.DefaultTraverse

final class TransDemo extends Properties("TransDemo") {
  import TransDemo._

  property("empty list to NelF fails") =
    toNelF(Fix[ListF[Int, *]](NilF)) ?= None

  property("round trip NelF") = {
    forAll { (nel: NonEmptyList[Int]) =>
      val listF = ListF.fromScalaList(nel.toList)
      toNelF(listF).map(fromNelF) ?= Some(listF)
    }
  }

}

object TransDemo {

  // non empty variant of ListF

  sealed trait NeListF[A, B]
  final case class NeLastF[A, B](value: A)         extends NeListF[A, B]
  final case class NeConsF[A, B](head: A, tail: B) extends NeListF[A, B]

  implicit def drosteTraverseForNeListF[A]: Traverse[NeListF[A, *]] =
    new DefaultTraverse[NeListF[A, *]] {
      def traverse[F[_]: Applicative, B, C](fb: NeListF[A, B])(
          f: B => F[C]): F[NeListF[A, C]] =
        fb match {
          case NeConsF(head, tail) => f(tail).map(NeConsF(head, _))
          case NeLastF(value)      => (NeLastF(value): NeListF[A, C]).pure[F]
        }
    }

  // converting a list to a non-empty list can fail, so we use TransM
  def transListToNeList[A]: TransM[
    Option,
    ListF[A, *],
    NeListF[A, *],
    Fix[ListF[A, *]]] = TransM {
    case ConsF(head, tail) =>
      Fix.un(tail) match {
        case NilF => NeLastF(head).some
        case _    => NeConsF(head, tail).some
      }
    case NilF => None
  }

  def toNelF[A]: Fix[ListF[A, *]] => Option[Fix[NeListF[A, *]]] =
    scheme.anaM(transListToNeList[A].coalgebra)

  // converting a non-empty list to a list can't fail, so we use Trans
  def transNeListToList[A]: Trans[
    NeListF[A, *],
    ListF[A, *],
    Fix[ListF[A, *]]] = Trans {
    case NeConsF(head, tail) => ConsF(head, tail)
    case NeLastF(last)       => ConsF(last, Fix[ListF[A, *]](NilF))
  }

  def fromNelF[A]: Fix[NeListF[A, *]] => Fix[ListF[A, *]] =
    scheme.cata(transNeListToList[A].algebra)

  // misc

  implicit def arbitraryNEL[A: Arbitrary]: Arbitrary[NonEmptyList[A]] =
    Arbitrary(for {
      head <- arbitrary[A]
      tail <- arbitrary[List[A]]
    } yield NonEmptyList.of(head, tail: _*))

}
