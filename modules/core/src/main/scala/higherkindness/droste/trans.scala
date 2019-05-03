package higherkindness.droste

import cats.Functor
import cats.syntax.functor._

abstract class GTrans[F[_], G[_], A, B] { self =>
  def apply(fa: F[A]): G[B]

  def algebra(implicit embed: Embed[G, B]): GAlgebra[F, A, B] =
    GAlgebra(x => embed.algebra(self(x)))

  def coalgebra(implicit project: Project[F, A]): GCoalgebra[G, A, B] =
    GCoalgebra(x => self(project.coalgebra(x)))

}

object GTrans {
  def apply[F[_], G[_], A, B](run: F[A] => G[B]): GTrans[F, G, A, B] =
    new GTrans[F, G, A, B] {
      def apply(fa: F[A]): G[B] = run(fa)
    }
}

abstract class GTransM[M[_], F[_], G[_], A, B] { self =>
  def apply(fa: F[A]): M[G[B]] 

  def algebra(
    implicit embed: Embed[G, B],
    ev: Functor[M]): GAlgebraM[M, F, A, B] =
    GAlgebraM(x => self(x).map(embed.algebra.apply))

  def coalgebra(implicit project: Project[F, A]): GCoalgebraM[M, G, A, B] =
    GCoalgebraM(x => self(project.coalgebra(x)))

}

object GTransM {
  def apply[M[_], F[_], G[_], A, B](run: F[A] => M[G[B]]) = 
    new GTransM[M, F, G, A, B] {
      def apply(fa: F[A]): M[G[B]] = run(fa)
    }
}
