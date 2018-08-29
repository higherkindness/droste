package qq.droste
package tests

import cats.kernel.{Monoid, Eq}
import cats.kernel.laws.discipline.MonoidTests

import org.scalacheck.{Properties, Arbitrary, Gen}
import org.scalacheck.Prop._

import qq.droste.data.list._
import qq.droste.data.Fix
import qq.droste.data.Mu
import qq.droste.data.Nu

final class ListTests extends Properties("ListTest") {

  property("Fix ListF -> List") = {
    val fixed: Fix[ListF[Int, ?]] =
      Fix(ConsF(1,
        Fix(ConsF(2,
          Fix(ConsF(3,
            Fix(NilF: ListF[Int, Fix[ListF[Int, ?]]])))))))

    ListF.toScalaList(fixed) ?= 1 :: 2 :: 3 :: Nil
  }

  property("Mu ListF -> List") = {
    val mu: Mu[ListF[Int, ?]] =
      Mu(ConsF(1,
        Mu(ConsF(2,
          Mu(ConsF(3,
            Mu(NilF: ListF[Int, Mu[ListF[Int, ?]]])))))))

    ListF.toScalaList(mu) ?= 1 :: 2 :: 3 :: Nil
  }

  property("Nu ListF -> List") = {
    val nu: Nu[ListF[Int, ?]] =
      Nu(ConsF(1,
        Nu(ConsF(2,
          Nu(ConsF(3,
            Nu(NilF: ListF[Int, Nu[ListF[Int, ?]]])))))))

    ListF.toScalaList(nu) ?= 1 :: 2 :: 3 :: Nil
  }

  property("rountrip List") = {
    // TODO: is there a way to rework/augment some of the schemes to return a
    // natural transformation valid for all lists?
    val f = scheme.hylo(ListF.toScalaListAlgebra[String], ListF.fromScalaListCoalgebra[String])
    forAll((list: List[String]) => f(list) ?= list)
  }

  implicit def arbitrary[T]: Arbitrary[Fix[ListF[Int, ?]]] =
    Arbitrary(Gen.listOf(Gen.posNum[Int]).map(ListF.fromScalaList[Int, Fix]))

  implicit def monoid: Monoid[Fix[ListF[Int, ?]]] = ListF.basisListFMonoid[Fix[ListF[Int, ?]], Int]

  implicit val eq: Eq[Fix[ListF[Int, ?]]] = ListF.basisListFEq[Fix[ListF[Int, ?]], Int]

  include(MonoidTests[Fix[ListF[Int, ?]]].monoid.all)
}
