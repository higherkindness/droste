package droste
package typeclass

import cats.Eval
import cats.free.Cofree

import alias._
import data.EnvT
import data.Fix

trait Embed[O[_]] {
  type Inn
  type Out[A] = O[A]

  def embed: Algebra[Out, Inn]
}

object Embed {

  def apply[O[_]](implicit ev: Embed[O]): Embed[O] = ev

  type Aux[O[_], I] = Embed[O] {
    type Inn = I
  }

  implicit def embedFromPattern[O[_], I](
    implicit ev: Pattern.Aux[O, I]): Aux[O, I] = ev
}

trait Project[O[_]] {
  type Inn
  type Out[A] = O[A]

  def project: Coalgebra[Out, Inn]
}

object Project {

  def apply[O[_]](implicit ev: Project[O]): Project[O] = ev

  type Aux[O[_], I] = Project[O] {
    type Inn = I
  }

  implicit def projectFromPattern[O[_], I](
    implicit ev: Pattern.Aux[O, I]): Aux[O, I] = ev
}

trait Pattern[O[_]] extends Embed[O] with Project[O] {
  override type Out[A] = O[A]
}

object Pattern extends PatternInstances0 {

  def apply[O[_]](implicit ev: Pattern[O]): Pattern[O] = ev

  type Aux[O[_], I] = Pattern[O] {
    type Inn = I
  }

  def instance[O[_], I](
    embed0  : Algebra[O, I],
    project0: Coalgebra[O, I]
  ): Aux[O, I] = new Pattern[O] {
    type Inn = I
    val embed   = embed0
    val project = project0
  }

}

private[typeclass] sealed trait PatternInstances0 extends PatternInstances1 { self: Pattern.type =>
  implicit def patternForEnvT[F[_], A]: Aux[EnvT[F, A, ?], Cofree[F, A]] =
    instance(
      fa => Cofree(fa.ask, Eval.now(fa.lower)),
      a  => EnvT(a.head, a.tail.value))

}
private[typeclass] sealed trait PatternInstances1 { self: Pattern.type =>
  implicit def patternForF[F[_]]: Aux[F, Fix[F]] =
    instance(Fix.fix, Fix.unfix)
}
