package qq.droste

import cats.Functor
import cats.syntax.functor._

import data._

object gather {

  def cata[F[_], A]: Gather[F, A, A] =
    (a, fa) => a

  def zygo[F[_]: Functor, A, B](algebra: Algebra[F, B]): Gather[F, A, (B, A)] =
    (a, fa) => (algebra(fa.map(_._1)), a)

  def para[F[_]: Functor, A, B](implicit embed: Embed[F, B]): Gather[F, A, (B, A)] =
    zygo(embed.algebra)

  def histo[F[_], A]: Gather[F, A, Cofree[F, A]] =
    Cofree(_, _)

}
