package qq.droste
package syntax

import cats.Applicative

object `package` {
  type ∘[F[_], G[_]] = { type λ[α] = F[G[α]] }

  implicit final class LiftArrowOps[A, B](val f: A => B) extends AnyVal {
    def lift[F[_]](implicit F: Applicative[F]): A => F[B] = a => F.pure(f(a))
  }
}
