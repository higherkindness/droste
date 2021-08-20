package higherkindness.droste
package util

import cats.Eval
import cats.Monoid
import cats.Traverse
import cats.arrow.Category
import cats.data.Const

trait DefaultTraverse[F[_]] extends Traverse[F] {
  override def foldMap[A, B: Monoid](fa: F[A])(f: A => B): B =
    traverse(fa)(a => Const[B, B](f(a))).getConst

  def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B =
    foldMap[A, List[A]](fa)(_ :: Nil)(Monoid[List[A]])
      .foldLeft(b)(f)

  def foldRight[A, B](fa: F[A], lb: Eval[B])(
      f: (A, Eval[B]) => Eval[B]
  ): Eval[B] =
    foldMap[A, Eval[B] => Eval[B]](fa)(a => lbb => Eval.defer(f(a, lbb)))(
      Category[Function1].algebra
    )(lb)
}
