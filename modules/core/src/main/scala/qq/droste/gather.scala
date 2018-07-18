package qq.droste

import data._

object gather {


  //gatherHisto :: Functor f => Gather f a (Cofree f a)
  //gatherHisto = (:<)

  def histo[F[_], A]: Gather[F, A, Cofree[F, A]] =
    Cofree(_, _)


}
