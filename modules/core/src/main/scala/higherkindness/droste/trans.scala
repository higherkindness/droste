package higherkindness.droste

import cats.Functor
import cats.syntax.functor._

final class GTrans[F[_], G[_], A, B](val run: F[A] => G[B]) extends AnyVal {
  def algebra(implicit embed: Embed[G, B]): GAlgebra[F, A, B] =
    a => embed.algebra(run(a))

  def coalgebra(implicit project: Project[F, A]): GCoalgebra[G, A, B] =
    GCoalgebra(project.coalgebra.run andThen run)
}

object GTrans {
  def apply[F[_], G[_], A, B](run: F[A] => G[B]): GTrans[F, G, A, B] =
    new GTrans(run)
}

final class GTransM[M[_], F[_], G[_], A, B](val run: F[A] => M[G[B]])
    extends AnyVal {
  def algebra(
      implicit embed: Embed[G, B],
      ev: Functor[M]): GAlgebraM[M, F, A, B] =
    GAlgebraM(run andThen (_.map(embed.algebra.run)))

  def coalgebra(implicit project: Project[F, A]): GCoalgebraM[M, G, A, B] =
    GCoalgebraM(project.coalgebra.run andThen run)
}

object GTransM {
  def apply[M[_], F[_], G[_], A, B](
      run: F[A] => M[G[B]]): GTransM[M, F, G, A, B] =
    new GTransM(run)
}
