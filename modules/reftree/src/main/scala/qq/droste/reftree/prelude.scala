package qq.droste
package reftree

import _root_.reftree.core.RefTree
import _root_.reftree.core.ToRefTree

import cats.Traverse

import data.prelude._
import data.CoenvT
import data.Cofree
import data.EnvT
import data.Fix
import data.Free

object prelude {

  implicit def fixedToRefTree[F[_] <: AnyRef: Traverse](
    implicit ev: ToRefTree[F[RefTree]]
  ): ToRefTree[Fix[F]] =
    ToRefTree(input =>
      scheme.hyloM[Zedd.M, F, Fix[F], RefTree](
        Zedd.up(fixedToRefTreeAlgebra),
        Zedd.down
      ).apply(input).runA(Zedd.empty).value
    )

  implicit def cofreeToRefTree[F[_] <: AnyRef: Traverse, A](
    implicit evF: ToRefTree[F[RefTree]], evA: ToRefTree[A]
  ): ToRefTree[Cofree[F, A]] =
    ToRefTree(input =>
      scheme.hyloM[Zedd.M, EnvT[A, F, ?], Cofree[F, A], RefTree](
        Zedd.up(cofreeToRefTreeAlgebra[F, A]),
        Zedd.down
      ).apply(input).runA(Zedd.empty).value
    )

  implicit def freeToRefTree[F[_] <: AnyRef: Traverse, A](
    implicit evF: ToRefTree[F[RefTree]], evA: ToRefTree[A]
  ): ToRefTree[Free[F, A]] =
    ToRefTree(input =>
      scheme.hyloM[Zedd.M, CoenvT[A, F, ?], Free[F, A], RefTree](
        Zedd.up(freeToRefTreeAlgebra[F, A]),
        Zedd.down
      ).apply(input).runA(Zedd.empty).value
    )

  private def fixedToRefTreeAlgebra[F[_]](
    implicit evF: ToRefTree[F[RefTree]]
  ): Algebra[F, RefTree] =
    Algebra((fa: F[RefTree]) => evF.refTree(fa))

  private def cofreeToRefTreeAlgebra[F[_] <: AnyRef, A](
    implicit evF: ToRefTree[F[RefTree]], evA: ToRefTree[A]
  ): Algebra[EnvT[A, F, ?], RefTree] =
    Algebra { (fa: EnvT[A, F, RefTree]) =>
      val children = evF.refTree(fa.lower) match {
        case ref: RefTree.Ref => ref.children.toList
        case other            => List(other.toField.withName("value"))
      }
      RefTree.Ref(fa.lower,
        evA.refTree(fa.ask).toField.withName("attr") :: children)
    }

  private def freeToRefTreeAlgebra[F[_] <: AnyRef, A](
    implicit evF: ToRefTree[F[RefTree]], evA: ToRefTree[A]
  ): Algebra[CoenvT[A, F, ?], RefTree] =
    Algebra(CoenvT.un(_).fold(evA.refTree, evF.refTree))
}
