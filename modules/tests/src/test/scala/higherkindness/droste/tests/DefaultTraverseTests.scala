package higherkindness.droste
package tests

import cats.implicits._
import higherkindness.droste.util.DefaultTraverse
import cats.Applicative
import cats.Eq
import cats.Traverse
import org.scalacheck.Arbitrary
import cats.laws.discipline.TraverseTests

import org.scalacheck.Properties

final case class Pair[A](l: A, r: A)

object Pair {

  implicit val traversePair: Traverse[Pair] = new DefaultTraverse[Pair] {
    def traverse[G[_], A, B](fa: Pair[A])(f: A => G[B])(
        implicit G: Applicative[G]): G[Pair[B]] =
      G.map2(f(fa.l), f(fa.r))(Pair(_, _))
  }

  implicit def eqPair[A](implicit eqA: Eq[A]): Eq[Pair[A]] =
    Eq.instance((p1, p2) => p1.l === p2.l && p1.r === p2.r)
}

final class DefaultTraverseTests extends Properties("DefaultTraverse") {
  implicit def arbPair[A](implicit arbA: Arbitrary[A]): Arbitrary[Pair[A]] =
    Arbitrary(
      arbA.arbitrary.flatMap(a1 => arbA.arbitrary.map(a2 => Pair(a1, a2))))

  include(
    TraverseTests[Pair].traverse[Int, Int, Int, Set[Int], Option, Option].all)
}
