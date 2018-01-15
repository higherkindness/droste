package droste
package data

import cats.Functor
import cats.syntax.functor._

final case class EnvT[F[_], E, A](ask: E, lower: F[A])

object EnvT {

  implicit def drosteFunctorForEnvT[F[_]: Functor, E]: Functor[EnvT[F, E, ?]] =
    new Functor[EnvT[F, E, ?]] {
      def map[A, B](fa: EnvT[F, E, A])(f: A => B): EnvT[F, E, B] =
        EnvT(fa.ask, fa.lower.map(f))
    }

}
