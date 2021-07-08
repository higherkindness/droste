package higherkindness.droste
package examples.futu

import org.scalacheck.Properties
import org.scalacheck.Prop._

import higherkindness.droste.data.list._
import higherkindness.droste.data.Coattr

final class ListExchange extends Properties("ListExchange") {

  // TODO: make this more ergonomic to write
  val exchangeCoalgebra: CVCoalgebra[ListF[String, *], List[String]] =
    CVCoalgebra {
      case Nil => NilF
      case head :: tail =>
        tail match {
          case Nil => ConsF(head, Coattr.pure(tail))
          case tailHead :: tailTail =>
            ConsF(
              tailHead,
              Coattr.roll(
                ConsF(
                  head,
                  Coattr.pure[ListF[String, *], List[String]](tailTail))))
        }
    }

  val f: List[String] => List[String] =
    scheme.zoo.futu(exchangeCoalgebra)

  property("simple pair wise swap check") = {
    val in: List[String]       = List("a", "b", "c", "d", "e", "f")
    val expected: List[String] = List("b", "a", "d", "c", "f", "e")
    f(in) ?= expected
  }

  property("pair wise swap") = forAll((in: List[String]) =>
    f(in) ?= in.sliding(2, 2).map(_.reverse).toList.flatten)

}
