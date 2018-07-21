package qq.droste
package tests

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop._
import org.scalacheck.Properties

import cats.Eq
import cats.implicits._
import cats.laws.discipline.ArrowTests
import cats.laws.discipline.eq._

final class AlgebraTests extends Properties("algebras") {

  implicit def galgebraEq[S, F[_], A](implicit FS: Arbitrary[F[S]], A: Eq[A]): Eq[GAlgebra[S, F, A]] =
    Eq.by[GAlgebra[S, F, A], F[S] => A](_.run)

  implicit def gcoalgebraEq[S, F[_], A](implicit A: Arbitrary[A], FS: Eq[F[S]]): Eq[GCoalgebra[S, F, A]] =
    Eq.by[GCoalgebra[S, F, A], A => F[S]](_.run)

  implicit def arbitraryGAlgebra[S, F[_], A](
    implicit arbA: Arbitrary[A], cogenFS: Cogen[F[S]]
  ): Arbitrary[GAlgebra[S, F, A]] =
    Arbitrary(Gen.function1(arbitrary[A]).map(GAlgebra(_)))

  implicit def arbitraryGCoalgebra[S, F[_], A](
    implicit arbFS: Arbitrary[F[S]], cogenA: Cogen[A]
  ): Arbitrary[GCoalgebra[S, F, A]] =
    Arbitrary(Gen.function1(arbitrary[F[S]]).map(GCoalgebra(_)))

  include(ArrowTests[GAlgebra[?, (Int, ?), ?]]
    .arrow[Int, Int, Int, Int, Int, Int].all, "GAlgebra.")

  include(ArrowTests[λ[(α, β) => GCoalgebra[β, (Int, ?), α]]]
    .arrow[Int, Int, Int, Int, Int, Int].all, "GCoalgebra.")

  property("GAlgebra round trip Cokleisli") =
    forAll((algebra: GAlgebra[Int, (Int, ?), Int]) =>
      algebra === GAlgebra(algebra.toCokleisli.run))

  property("GCoalgebra round trip Kleisli") =
    forAll((coalgebra: GCoalgebra[Int, (Int, ?), Int]) =>
      coalgebra === GCoalgebra(coalgebra.toKleisli.run))


}
