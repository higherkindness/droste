package qq.droste

import cats.Functor
import cats.syntax.functor._

final class Trans[F[_], G[_], A](val run: F[A] => G[A]) extends AnyVal {
  def algebra(implicit embed: Embed[G, A]): Algebra[F, A] =
    Algebra(run andThen embed.algebra.run)

  def coalgebra(implicit project: Project[F, A]): Coalgebra[G, A] =
    Coalgebra(project.coalgebra.run andThen run)
}

object Trans {
  def apply[F[_], G[_], A](run: F[A] => G[A]): Trans[F, G, A] =
    new Trans(run)
}

final class TransM[M[_], F[_], G[_], A](val run: F[A] => M[G[A]]) extends AnyVal {
  def algebra(implicit embed: Embed[G, A], ev: Functor[M]): AlgebraM[M, F, A] =
    AlgebraM(run andThen (_.map(embed.algebra.run)))

  def coalgebra(implicit project: Project[F, A]): CoalgebraM[M, G, A] =
    CoalgebraM(project.coalgebra.run andThen run)
}

object TransM {
  def apply[M[_], F[_], G[_], A](run: F[A] => M[G[A]]): TransM[M, F, G, A] =
    new TransM(run)
}
