package droste
package data
package tree

import alias.Algebra
import alias.Coalgebra
import typeclass._

import cats.Eval
import cats.Functor
import cats.Show
import cats.free.Cofree
import cats.implicits._

object `package` {

  type Tree[A]     = Cofree[Stream, A]
  type TreeF[A, B] = EnvT[Stream, A, B]

  implicit def drosteToTreeOps[A](tree: Tree[A]): TreeOps[A] =
    new TreeOps(tree)

  implicit def drosteToTreeFOps[A, B](tree: TreeF[A, B]): TreeFOps[A, B] =
    new TreeFOps(tree)

}

object Node {
  def apply[A](label: A, forest: Stream[Tree[A]]): Tree[A] =
    Cofree(label, Eval.now(forest))

  def apply[A](label: A, forest: Tree[A]*): Tree[A] =
    Cofree(label, Eval.now(forest.toStream))
}

object Leaf {
  def apply[A](label: A): Tree[A] =
    Cofree(label, Eval.now(Stream.empty[Tree[A]]))
}

class TreeOps[A](val tree: Tree[A]) extends AnyVal {
  def label: A = tree.head
  def forest: Stream[Tree[A]] = tree.tail.value

  def fixed: Fix[TreeF[A, ?]] = {
    // TODO: No way to unfold to Fix without specifying everything manually
    val P = implicitly[Embed.Aux[TreeF[A, ?], Fix[TreeF[A, ?]]]]
    val F = Functor[TreeF[A, ?]]
    scheme.ana(TreeF.fromTree[A])(F, P).apply(tree)
  }
}

object TreeF {

  def toTree[A]: Algebra[TreeF[A, ?], Tree[A]] =
    tree => Node(tree.label, tree.forest)

  def fromTree[A]: Coalgebra[TreeF[A, ?], Tree[A]] =
    tree => NodeF(tree.label, tree.forest)

  def render[A: Show](tree: Fix[TreeF[A, ?]]): String = {
    val res = scheme.cata(tree)(renderAlgebra[A])
    res.tail.foldLeft(new StringBuilder(res.head.toString.reverse))((acc, entry) =>
      acc.append("\n").append(entry.reverse)
    ).toString
  }

  private[this] def renderAlgebra[A: Show]: Algebra[TreeF[A, ?], Vector[StringBuilder]] = { tree =>
    val sbl = new StringBuilder(tree.label.show.reverse)
    if (tree.forest.isEmpty) Vector[StringBuilder](sbl)
    else {
      val forestVector = tree.forest.toVector
      val last = forestVector.length - 1
      sbl +: forestVector
        .mapWithIndex { (subtree, index) =>
          if (index == last)
            renderChildren(subtree, " ─└ ", "   ", " │ ")
          else
            renderChildren(subtree, " ─├ ", " │ ", " │ ")
        }
        .flatten
    }
  }

  private[this] def renderChildren(
    forest: Vector[StringBuilder],
    first: String, last: String, default: String
  ): Vector[StringBuilder] = {
    val end: Int = forest.length - 1
    forest(0).append(first)
    if (end > 0) forest(end).append(last)
    var i: Int = 1
    while (i < end) {
      forest(i).append(default)
      i += 1
    }
    forest
  }

}

object NodeF {
  def apply[A, B](label: A, forest: Stream[B]): TreeF[A, B] =
    EnvT(label, forest)

  def apply[A, B](label: A, forest: B*): TreeF[A, B] =
    EnvT(label, forest.toStream)
}

object LeafF {
  def apply[A, B](label: A): TreeF[A, B] =
    EnvT(label, Stream.empty)
}

class TreeFOps[A, B](val tree: TreeF[A, B]) extends AnyVal {
  def label: A = tree.ask
  def forest: Stream[B] = tree.lower
}
