package higherkindness.droste
package tests

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
import cats.syntax.all._
import cats.kernel.laws.SemigroupLaws
import org.typelevel.discipline._
import org.scalacheck.Prop
import scala.collection.immutable.SortedMap
import org.typelevel.discipline.AllProperties
import cats.kernel.laws.MonoidLaws
import cats.kernel.instances.option._
import cats.kernel.Eq
import cats.kernel.laws.IsEq

final class StreamTests extends Properties("StreamTests") {

  implicit class OverridableRuleSet(rs: Laws#RuleSet) extends Laws {
    private def filteredParentProps(rs: Laws#RuleSet, f: (String, Prop) => Boolean): SortedMap[String, Prop] = SortedMap(filterProps(f, rs.props): _*) ++ rs.parents.flatMap(ruleset => filteredParentProps(ruleset, f))

    private def filterProps(f: (String, Prop) => Boolean, props: Seq[(String, Prop)]): Seq[(String, Prop)] = props.filter(f.tupled)

    private def filteredBasesRuleSets(rs: Laws#RuleSet, f: (String, Prop) => Boolean): Seq[(String, Laws#RuleSet)] = rs.bases.map{ case (baseName, ruleset) =>
      name -> new RuleSet {
        val parents: Seq[RuleSet] = Nil
        val name = baseName
        val bases = filteredBasesRuleSets(ruleset, f)
        val props = filterProps(f, ruleset.props)
      }
    }

    def overrideProps(props: (String, Prop)*): Properties = {
      val propMap = props.toMap
      val overrideFilter: (String, Prop) => Boolean = (name, _) => !propMap.contains(name)
      val filteredParents = filteredParentProps(rs, overrideFilter)
      val filteredBases = filteredBasesRuleSets(rs, overrideFilter)
      new AllProperties(rs.name, filteredBases, filteredParents ++ SortedMap(props.map{ case (name, prop) => s"$name (Overrided)" -> prop}: _*))
    }
  }

  import Stream.implicits._
  import Stream._
  import ListF._

  property("Stream.forever") = forAll(
    Gen.chooseNum(-1000, 1000),
    arbitrary[String]
  )((n, v) => Stream.forever(v).take(n).toList ?= List.fill(n)(v))

  property("Stream.map") = forAll((f: Int => String) =>
    Stream.naturalNumbers.map(f).take(100).toList ?= List
      .iterate(1, 100)(_ + 1)
      .map(f)
  )

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

  property("Stream.fromIterator") =
    forAll((l: List[String]) => Stream.fromIterator(l.iterator).toList ?= l)

  implicit val cogenIntStream: Cogen[Stream[Int]] =
    Cogen.cogenList[Int].contramap(_.toList)

  implicit val arbitraryIntStream: Arbitrary[Stream[Int]] =
    Arbitrary(
      Gen
        .resize(25, Gen.listOf(Gen.chooseNum[Int](1, Int.MaxValue)))
        .map(Stream.fromList)
    )

  include(EqTests[Stream[Int]].eqv.all)

  val streamGen = implicitly[Arbitrary[Stream[Int]]].arbitrary
  implicit val limitedVectorGen: Arbitrary[Vector[Stream[Int]]] = Arbitrary(Gen.listOfN(10, streamGen).map(_.toVector))

  implicit def isEqfromEq[A: Eq]: IsEq[A] => Prop = iseq => iseq.lhs === iseq.rhs

  val semigroupLaws = SemigroupLaws[Stream[Int]]
  val monoidLaws = MonoidLaws[Stream[Int]]

  include(MonoidTests[Stream[Int]].monoid.overrideProps(
    "combine all" -> forAll(monoidLaws.combineAll _),
    "combineAllOption" -> forAll(semigroupLaws.combineAllOption _),
    "reverseCombineAllOption" -> forAll(semigroupLaws.reverseCombineAllOption _),
    "intercalateCombineAllOption" -> forAll(semigroupLaws.intercalateCombineAllOption _)
  ))

}
