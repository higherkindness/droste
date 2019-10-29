package higherkindness.droste
package tests

import cats.instances.int._
import cats.kernel.laws.discipline.{MonoidTests, EqTests}

import org.scalacheck.{Properties, Arbitrary, Gen, Cogen}
import org.scalacheck.Prop._

import higherkindness.droste.prelude._
import higherkindness.droste.data.list._
import higherkindness.droste.data.Fix
import higherkindness.droste.data.Mu
import higherkindness.droste.data.Nu

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

  implicit def cogen[T](implicit T: Basis[ListF[Int, ?], T]): Cogen[T] =
    Cogen.cogenList[Int].contramap { x =>
      val toList: T => List[Int] = scheme.hylo[ListF[Int, ?], T, List[Int]](ListF.toScalaListAlgebra[Int], T.coalgebra)
      toList(x)
    }
  implicit def arbitrary[T](implicit T: Basis[ListF[Int, ?], T]): Arbitrary[T] =
    Arbitrary(Gen.resize(25, Gen.listOf(Gen.chooseNum[Int](1, Int.MaxValue))).map(scheme.ana(ListF.fromScalaListCoalgebra[Int])))

  import ListF._

  include(MonoidTests[Fix[ListF[Int, ?]]].monoid.all)
  include(EqTests[Fix[ListF[Int, ?]]].eqv.all)
}
