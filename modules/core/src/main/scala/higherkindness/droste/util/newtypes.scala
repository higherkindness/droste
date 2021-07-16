package higherkindness.droste
package util

import cats.Monoid

object newtypes {

  final case class @@[A, B](unwrap: A)

  object Tags {
    sealed trait Conjunction
    sealed trait Disjunction
    sealed trait First
  }

  implicit class BooleanOps(b: Boolean) {
    def conjunction: Boolean @@ Tags.Conjunction = @@(b)
    def disjunction: Boolean @@ Tags.Disjunction = @@(b)
  }

  implicit val conjunctionMonoid: Monoid[Boolean @@ Tags.Conjunction] =
    new Monoid[Boolean @@ Tags.Conjunction] {
      def empty: Boolean @@ Tags.Conjunction = @@(true)
      def combine(
          a: Boolean @@ Tags.Conjunction,
          b: Boolean @@ Tags.Conjunction): Boolean @@ Tags.Conjunction =
        @@(a.unwrap && b.unwrap)
    }

  implicit val disjunctionMonoid: Monoid[Boolean @@ Tags.Disjunction] =
    new Monoid[Boolean @@ Tags.Disjunction] {
      def empty: Boolean @@ Tags.Disjunction = @@(false)
      def combine(
          a: Boolean @@ Tags.Disjunction,
          b: Boolean @@ Tags.Disjunction): Boolean @@ Tags.Disjunction =
        @@(a.unwrap || b.unwrap)
    }

  implicit class OptionOps[B](o: Option[B]) {
    def first: Option[B] @@ Tags.First = @@(o)
  }

  implicit def optionMonoid[B]: Monoid[Option[B] @@ Tags.First] =
    new Monoid[Option[B] @@ Tags.First] {
      def empty: Option[B] @@ Tags.First = @@(Option.empty[B])
      def combine(x: Option[B] @@ Tags.First, y: Option[B] @@ Tags.First): Option[B] @@ Tags.First = @@(x.unwrap.orElse(y.unwrap))
    }
}
