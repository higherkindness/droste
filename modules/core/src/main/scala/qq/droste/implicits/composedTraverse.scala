package qq.droste
package implicits

import cats.Traverse
import syntax.compose._

object composedTraverse {
  implicit def drosteComposedTraverse[F[_], G[_]](
      implicit F: Traverse[F],
      G: Traverse[G]
  ): Traverse[(F ∘ G)#λ] = F compose G
}
