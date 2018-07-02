package qq.droste
package implicits

import cats.Functor
import syntax._

object composedFunctor {
  implicit def drosteComposedFunctor[F[_], G[_]](
    implicit F: Functor[F], G: Functor[G]
  ): Functor[(F ∘ G)#λ] = F compose G
}