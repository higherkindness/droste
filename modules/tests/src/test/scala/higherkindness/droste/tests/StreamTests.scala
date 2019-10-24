package higherkindness.droste
package tests

import cats.instances.int._
import cats.kernel.laws.discipline.EqTests
import cats.kernel.laws.discipline.MonoidTests

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Cogen
import org.scalacheck.Properties
import org.scalacheck.Prop._

import higherkindness.droste.prelude._
import higherkindness.droste.data.stream._
import higherkindness.droste.data.list.ListF

final class StreamTests extends Properties("StreamTests") {

  import Stream.implicits._
  import Stream._
  import ListF._

  property("Stream.forever") = forAll(
    Gen.chooseNum(-1000, 1000),
    arbitrary[String]
  )((n, v) => Stream.forever(v).take(n).toList ?= List.fill(n)(v))

  property("Stream.map") = forAll(
    (f: Int => String) =>
      Stream.naturalNumbers.map(f).take(100).toList ?= List
        .iterate(1, 100)(_ + 1)
        .map(f))

  property("Stream.flatMap") = {
    val res = Stream.naturalNumbers
      .flatMap(n => Stream.naturalNumbers.take(n))
      .take(5000)
      .toList
    res ?= List
      .iterate(1, 100)(_ + 1)
      .flatMap(n => List.iterate(1, n)(_ + 1))
      .take(5000)
  }

  property("Stream.fromIterator") = forAll(
    (l: List[String]) => Stream.fromIterator(l.iterator).toList ?= l)

  implicit val cogenIntStream: Cogen[Stream[Int]] =
    Cogen.cogenList[Int].contramap(_.toList)

  implicit val arbitraryIntStream: Arbitrary[Stream[Int]] =
    Arbitrary(
      Gen
        .resize(50, Gen.listOf(Gen.chooseNum[Int](1, Int.MaxValue)))
        .map(Stream.fromList))

  include(EqTests[Stream[Int]].eqv.all)
  include(MonoidTests[Stream[Int]].monoid.all)

}
