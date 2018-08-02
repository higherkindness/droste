package qq.droste
package examples.futu

import org.scalacheck.Properties
import org.scalacheck.Prop._

import qq.droste.data.list._
import qq.droste.data.Fix
import qq.droste.data.Free

final class ListExchange extends Properties("ListExchange") {

  type FixedList[A] = Fix[ListF[A, ?]]

  // TODO: make this more ergonomic to write
  val exchangeCoalgebra: CVCoalgebra[ListF[String, ?], FixedList[String]] = CVCoalgebra(Fix.un(_) match {
    case NilF => NilF
    case ConsF(head, tail) => Fix.un(tail) match {
      case NilF => ConsF(head, Free.pure(tail))
      case ConsF(tailHead, tailTail) =>
        ConsF(tailHead, Free.roll(
          ConsF(head, Free.pure[ListF[String, ?], FixedList[String]](tailTail))))
    }
  })

  val f: List[String] => List[String] =
    scheme.zoo.futu(exchangeCoalgebra) andThen
    (ListF.toScalaList(_)) compose
    (ListF.fromScalaList(_))

  property("simple pair wise swap check") = {
    val in: List[String] = List("a", "b", "c", "d", "e", "f")
    val expected: List[String] = List("b", "a", "d", "c", "f", "e")
    f(in) ?= expected
  }

  property("pair wise swap") =
    forAll((in: List[String]) =>
      f(in) ?= in.sliding(2, 2).map(_.reverse).toList.flatten)

}
