package higherkindness.droste.examples.paraM

import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.Monad
import cats.syntax.functor._
import cats.instances.option._

import higherkindness.droste.data.list._
import higherkindness.droste.RAlgebraM
import higherkindness.droste.scheme

final class PartOrder extends Properties("PartOrder") {

  import PartOrder._

  def insertTuple(
      x: (Int, Int),
      l: List[(Int, Int)]): Option[List[(Int, Int)]] =
    insertM[Option, (Int, Int)]({
      case ((x1, y1), (x2, y2)) =>
        if ((x1 <= x2) && (y1 <= y2)) Some(true)
        else if ((x1 > x2) && (y1 > y2)) Some(false)
        else None
    }, x).apply(l)

  property("empty insert") =
    insertTuple((0, 0), Nil) ?= Some(List((0, 0)))

  property("non-empty insert") =
    insertTuple((2, 2), List((0, 0), (1, 1), (3, 3))) ?= Some(
      List((0, 0), (1, 1), (2, 2), (3, 3)))

  property("failing insert") =
    insertTuple((1, 1), List((0, 0), (0, 2), (2, 2))) ?= None

}

object PartOrder {

  // insert a value into a list using an effectful comparison
  def insertM[M[_], A](cmp: (A, A) => M[Boolean], x: A)(
      implicit M: Monad[M]): List[A] => M[List[A]] =
    scheme.zoo.paraM(
      RAlgebraM[List[A], M, ListF[A, *], List[A]] {
        case NilF => M.pure(List(x))
        case ConsF(h, (l, rec)) =>
          cmp(x, h).map(if (_) x :: h :: l else h :: rec)
      }
    )

}
