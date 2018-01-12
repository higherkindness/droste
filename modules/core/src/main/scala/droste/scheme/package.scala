package droste
package scheme

import cats.~>
import cats.Functor
import cats.Monad
import cats.Traverse
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._

import alias._
import typeclass._
import syntax.functork._

object `package` {

  def hylo[F[_]: Functor, A, B]
    (
      fold  : Algebra  [F, B],
      unfold: Coalgebra[F, A]
    )
      : A => B =
    new (A => B) { def apply(a: A): B =
      fold(unfold(a).map(this)) }


  def hyloM[M[_]: Monad, F[_]: Traverse, A, B]
    (
      fold  : AlgebraM  [M, F, B],
      unfold: CoalgebraM[M, F, A]
    )
      : A => M[B] =
    hylo[λ[α => M[F[α]]], A, M[B]](
      _.flatMap(_.sequence.flatMap(fold)),
      unfold)(Functor[M] compose Functor[F])


  def hyloK[F[_[_], _]: FunctorK, A[_], B[_]]
    (
      fold  : AlgebraK  [F, B],
      unfold: CoalgebraK[F, A]
    )
      : A ~> B =
    new (A ~> B) { def apply[Δ](a: A[Δ]): B[Δ] =
      fold(unfold(a).mapK(this)) }


  def ana[A, Base[_]]
    (
      unfold: Coalgebra[Base, A]
    )
    (implicit ev: Functor[Base], B: Embed[Base])
      : A => B.Inn =
    hylo(B.embed, unfold)


  def cata[A, Base[_]]
    (
      fold  : Algebra[Base, A]
    )
    (implicit ev: Functor[Base], B: Project[Base])
      : B.Inn => A =
    hylo(fold, B.project)

}
