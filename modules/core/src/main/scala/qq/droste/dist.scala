package qq.droste

import cats.~>
import cats.Functor
import cats.syntax.functor._

import data.prelude._
import data.Cofree
import syntax.alias._

object dist {

  def cofree[F[_]: Functor]: (F ∘ Cofree[F, ?])#λ ~> (Cofree[F, ?] ∘ F)#λ =
    λ[(F ∘ Cofree[F, ?])#λ ~> (Cofree[F, ?] ∘ F)#λ](
      Cofree.ana(_)(_.map(_.tail), _.map(_.head)))

}
