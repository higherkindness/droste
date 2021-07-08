package higherkindness.droste.examples

import org.scalacheck.Properties
import org.scalacheck.Prop._

import cats.Applicative
import cats.Traverse
import cats.syntax.all._

import higherkindness.droste.CoalgebraM
import higherkindness.droste.scheme
import higherkindness.droste.data.Nu
import higherkindness.droste.util.DefaultTraverse

import scala.annotation.tailrec

final class NuLookup extends Properties("NuLookup") {
  import NuLookup._

  val lookup: Map[String, Result[String]] = Map(
    "a"          -> Ref("b"),
    "b"          -> Ref("c"),
    "this"       -> Ref("is"),
    "is"         -> Ref("broken"),
    "c"          -> Ref("d"),
    "d"          -> Value("d-value"),
    "work it"    -> Ref("harder"),
    "harder"     -> Ref("make it"),
    "make it"    -> Ref("better"),
    "better"     -> Ref("do it"),
    "do it"      -> Ref("faster"),
    "faster"     -> Ref("makes us"),
    "makes us"   -> Ref("more than"),
    "more than"  -> Ref("ever"),
    "ever"       -> Ref("hour after"),
    "hour after" -> Ref("hour"),
    "hour"       -> Ref("work is"),
    "work is"    -> Ref("never"),
    "never"      -> Value("over"),
    "loop a"     -> Ref("loop b"),
    "loop b"     -> Ref("loop a")
  )

  def catchAll[A](f: => A): Either[Throwable, A] =
    try {
      Right(f)
    } catch {
      case t: Throwable => Left(t)
    }

  property("Nu.project") = {

    // Note that we return our result in _one_ level of Option,
    // meaning that everything is unfolded to Nu when f is invoked
    val f: String => Option[Nu[Result]] =
      scheme.anaM[Option, Result, String, Nu[Result]](
        CoalgebraM(lookup.get(_: String)))

    @tailrec def unroll(nu: Nu[Result]): String = {
      Nu.un(nu) match {
        case Ref(a)   => unroll(a)
        case Value(v) => v
      }
    }

    val p1 = f("a").map(unroll) ?= Some("d-value")
    val p2 = f("z").map(unroll) ?= None
    val p3 = f("work it").map(unroll) ?= Some("over")
    val p4 = f("this").map(unroll) ?= None
    val p5 = catchAll(f("loop a")).isLeft // stackoverflow :(

    p1 && p2 && p3 && p4 && p5
  }

}

object NuLookup {
  sealed trait Result[A]
  final case class Ref[A](next: A)         extends Result[A]
  final case class Value[A](value: String) extends Result[A]

  object Result {
    implicit val resultTraverse: Traverse[Result] =
      new DefaultTraverse[Result] {
        def traverse[G[_]: Applicative, A, B](fa: Result[A])(
            f: A => G[B]): G[Result[B]] =
          fa match {
            case Ref(next)              => f(next).map(Ref(_))
            case v: Value[B @unchecked] => (v: Result[B]).pure[G]
          }
      }
  }
}
