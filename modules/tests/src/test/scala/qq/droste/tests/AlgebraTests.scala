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

  implicit def galgebraEq[F[_], S, A](
      implicit FS: Arbitrary[F[S]],
      A: Eq[A]): Eq[GAlgebra[F, S, A]] =
    Eq.by[GAlgebra[F, S, A], F[S] => A](_.run)

  implicit def gcoalgebraEq[F[_], A, S](
      implicit A: Arbitrary[A],
      FS: Eq[F[S]]): Eq[GCoalgebra[F, A, S]] =
    Eq.by[GCoalgebra[F, A, S], A => F[S]](_.run)

  implicit def arbitraryGAlgebra[F[_], S, A](
      implicit arbA: Arbitrary[A],
      cogenFS: Cogen[F[S]]
  ): Arbitrary[GAlgebra[F, S, A]] =
    Arbitrary(Gen.function1(arbitrary[A]).map(GAlgebra(_)))

  implicit def arbitraryGCoalgebra[F[_], A, S](
      implicit arbFS: Arbitrary[F[S]],
      cogenA: Cogen[A]
  ): Arbitrary[GCoalgebra[F, A, S]] =
    Arbitrary(Gen.function1(arbitrary[F[S]]).map(GCoalgebra(_)))

  include(
    ArrowTests[GAlgebra[(Int, ?), ?, ?]]
      .arrow[Int, Int, Int, Int, Int, Int]
      .all,
    "GAlgebra.")

  include(
    ArrowTests[GCoalgebra[(Int, ?), ?, ?]]
      .arrow[Int, Int, Int, Int, Int, Int]
      .all,
    "GCoalgebra.")

  property("GAlgebra round trip Cokleisli") = forAll(
    (algebra: GAlgebra[(Int, ?), Int, Int]) =>
      algebra === GAlgebra(algebra.toCokleisli.run))

  property("GCoalgebra round trip Kleisli") = forAll(
    (coalgebra: GCoalgebra[(Int, ?), Int, Int]) =>
      coalgebra === GCoalgebra(coalgebra.toKleisli.run))

}
