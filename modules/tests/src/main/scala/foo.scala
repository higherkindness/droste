import qq.droste._
import qq.droste.data._
import qq.droste.data.prelude._

import cats.free.Coyoneda

object TestApp extends App {

  type FreeF[F[_], A, B] = CoattrF[Coyoneda[F, ?], A, B]
  type Free[F[_], A] = Mu[FreeF[F, A, ?]]

  sealed trait Op[A]
  object Op {
    final case class Add(x: Int, y: Int) extends Op[Int]
    final case class Prod(x: Int, y: Int) extends Op[Int]
  }

  val program: Free[Op, Int] = Mu(CoattrF.pure(1))

  def attribute[F[_], A]: Free[F, A] => Free[F, (Int, A)] =
    scheme.cata(Algebra[FreeF[F, A, ?], Free[F, (Int, A)]](ffa =>
      CoattrF.un(ffa) match {
        case Left(a) => Mu(CoattrF.pure(1 -> a))
        case Right(z) => Mu(CoattrF.roll(z))
      }
    ))

  println(program)
  println(attribute(program))


}
