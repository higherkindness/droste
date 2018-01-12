package droste
package scheme

import alias._
import data.Fix

import cats.Functor

import org.scalacheck.Properties
import org.scalacheck.Prop._

class SchemeTests extends Properties("scheme") {

  sealed trait ListF[+A, +B]
  case class ConsF[A, B](head: A, tail: B) extends ListF[A, B]
  case object NilF extends ListF[Nothing, Nothing]

  object ListF {
    implicit def functorInstance[A]: Functor[ListF[A, ?]] =
      new Functor[ListF[A, ?]] {
        def map[B, C](fb: ListF[A, B])(f: B => C): ListF[A, C] =
          fb match {
            case ConsF(head, tail) => ConsF(head, f(tail))
            case NilF => NilF
          }
      }
  }

  val fold: Algebra[ListF[String, ?], String] = {
    case ConsF(head, tail) => s"$head$tail"
    case NilF              => ""
  }

  val unfold: Coalgebra[ListF[String, ?], String] =
    input =>
      if (input.isEmpty) NilF
      else ConsF(input.take(1), input.drop(1))

  val fixed: String => Fix[ListF[String, ?]] =
    _.grouped(1).foldRight(Fix.fix[ListF[String, ?]](NilF))((c, acc) =>
      Fix.fix(ConsF(c, acc)))

  property("hylo <-> Fix") = {
    val f = hylo(fold, unfold)

    forAll((input: String) =>
      f(input) ?= input)
  }

  property("ana  --> Fix") = {
    val f = ana(unfold)

    forAll((input: String) =>
      f(input) ?= fixed(input))
  }

  property("cata <-- Fix") = {
    val f = cata(fold)

    forAll((input: String) =>
      f(fixed(input)) ?= input)
  }

}
