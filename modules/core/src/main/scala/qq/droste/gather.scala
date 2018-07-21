package qq.droste

import cats.Functor
import cats.syntax.functor._

import data._

object Gather {

  def cata[F[_], A]: Gather[A, F, A] =
    (a, fa) => a

  def zygo[F[_]: Functor, A, B](algebra: Algebra[F, B]): Gather[(B, A), F, A] =
    (a, fa) => (algebra(fa.map(_._1)), a)

  def para[F[_]: Functor, A, B](implicit embed: Embed[F, B]): Gather[(B, A), F, A] =
    zygo(embed.algebra)

  def histo[F[_], A]: Gather[Cofree[F, A], F, A] =
    Cofree(_, _)

  def zip[F[_]: Functor, Ax, Ay, Sx, Sy](
    x: Gather[Sx, F, Ax],
    y: Gather[Sy, F, Ay]
  ): Gather[(Sx, Sy), F, (Ax, Ay)] =
    (a, fs) => (x(a._1, fs.map(_._1)), y(a._2, fs.map(_._2)))

}
