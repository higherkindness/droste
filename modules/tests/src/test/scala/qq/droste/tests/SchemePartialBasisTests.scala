package qq.droste
package tests

import org.scalacheck.Properties
import org.scalacheck.Prop._

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import eu.timepit.refined.scalacheck.numeric._

import cats.Eval
import cats.instances.option._

import data.prelude._
import data.Attr
import data.Fix
import data.Mu
import data.Nu
import syntax.attr._

class SchemePartialBasisTests extends Properties("SchemePartialBasis") {

  property("scheme[Fix].ana") = {

    val f = scheme[Fix].ana(
      Coalgebra((n: Int) => if (n > 0) Some(n - 1) else None))

    def expected(n: Int): Fix[Option] =
      if (n > 0) Fix(Some(expected(n - 1)))
      else Fix(None: Option[Fix[Option]])

    forAll((n: Int Refined Less[W.`100`.T]) => f(n) ?= expected(n))
  }

  property("scheme[Attr[?[_], Int]].ana") = {

    val f = scheme[Attr[?[_], Int]].ana(
      Coalgebra((n: Int) => (if (n > 0) Some(n - 1) else None) attr n))

    def expected(n: Int): Attr[Option, Int] =
      if (n > 0) Attr(n, Some(expected(n - 1)))
      else Attr(n, None: Option[Attr[Option, Int]])

    forAll((n: Int Refined Less[W.`100`.T]) => f(n) ?= expected(n))
  }

  property("scheme[cats.free.Cofree[?[_], Int]].ana") = {

    val f = scheme[cats.free.Cofree[?[_], Int]].ana(
      Coalgebra((n: Int) => (if (n > 0) Some(n - 1) else None) attr n))

    def expected(n: Int): cats.free.Cofree[Option, Int] =
      if (n > 0) cats.free.Cofree(n, Eval.now(Some(expected(n - 1))))
      else cats.free.Cofree(n, Eval.now(None: Option[cats.free.Cofree[Option, Int]]))

    forAll((n: Int Refined Less[W.`100`.T]) => f(n) ?= expected(n))
  }

  property("scheme[Mu].ana") = {

    val f = scheme[Mu].ana(
      Coalgebra((n: Int) => if (n > 0) Some(n - 1) else None))

    def expected(n: Int): Mu[Option] =
      if (n > 0) Mu(Some(expected(n - 1)))
      else Mu(None: Option[Mu[Option]])

    forAll((n: Int Refined Less[W.`100`.T]) => f(n) ?= expected(n))
  }

  property("scheme[Nu].ana") = {

    val f = scheme[Nu].ana(
      Coalgebra((n: Int) => if (n > 0) Some(n - 1) else None))

    def expected(n: Int): Nu[Option] =
      if (n > 0) Nu(Some(expected(n - 1)))
      else Nu(None: Option[Nu[Option]])

    forAll((n: Int Refined Less[W.`100`.T]) => f(n) ?= expected(n))
  }

}
