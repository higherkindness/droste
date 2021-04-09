package higherkindness.droste.examples.apoM

import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.Monad
import cats.syntax.functor._
import cats.instances.option._

import higherkindness.droste.data.list._
import higherkindness.droste.RCoalgebraM
import higherkindness.droste.scheme

final class HeadEff extends Properties("HeadEff") {

  import HeadEff._

  def reciprocalHd(xs: List[Double]): Option[List[Double]] =
    mapHeadM[Option, Double](x => if (x != 0) Some(1.0 / x) else None).apply(xs)

  property("empty reciprocal") =
    reciprocalHd(Nil) ?= Some(Nil)

  property("non-empty reciprocal") =
    reciprocalHd(List(2.0, 3.0, 4.0)) ?= Some(List(0.5, 3.0, 4.0))

  property("failing reciprocal") =
    reciprocalHd(List(0.0, 3.0, 4.0)) ?= None

}

object HeadEff {

  // map the list head using an effectful function
  def mapHeadM[M[_], A](f: A => M[A])(
      implicit M: Monad[M]): List[A] => M[List[A]] =
    scheme.zoo.apoM(
      RCoalgebraM[List[A], M, ListF[A, *], List[A]] {
        case Nil    => M.pure(NilF)
        case h :: t => f(h).map(ConsF(_, Left(t)))
      }
    )

}
