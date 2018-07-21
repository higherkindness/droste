package qq.droste

import cats.Functor
import cats.syntax.functor._

object Scatter {

  def ana[F[_], A]: Scatter[A, F, A] =
    Left(_)

  def gapo[F[_]: Functor, A, B](coalgebra: Coalgebra[F, B]): Scatter[Either[B, A], F, A] = {
    case Left(b) => Right(coalgebra(b).map(Left(_)))
    case Right(a) => Left(a)
  }

  def apo[F[_]: Functor, A, B](implicit project: Project[F, B]): Scatter[Either[B, A], F, A] =
    gapo(project.coalgebra)
}
