package higherkindness.droste
package syntax

import cats.Applicative
import cats.Foldable
import cats.Monad
import cats.kernel.Monoid
import cats.kernel.Eq

import data.AttrF
import data.Fix
import data.list._

object all
    extends ComposeSyntax
    with AttrSyntax
    with LiftSyntax
    with FixSyntax
    with UnfixSyntax
    with EmbedSyntax
    with ProjectSyntax

object compose extends ComposeSyntax
object attr    extends AttrSyntax
object lift    extends LiftSyntax
object fix     extends FixSyntax
object unfix   extends UnfixSyntax
object embed   extends EmbedSyntax
object project extends ProjectSyntax

sealed trait ComposeSyntax {

  /** Compose two functors `F` and `G`.
    *
    * This allows you to inline what would otherwise require
    * a type alias.
    *
    * Consider the method
    * {{{
    * def method[F[_]]: Unit = ()
    * }}}
    *
    * Calling method with a simple type is easy:
    * {{{
    * method[Option]
    * }}}
    *
    * Calling method with a nested type is more complicated.
    * The traditional way to do this is to use an alias:
    * {{{
    * type ListOption[A] = List[Option[A]]
    * method[ListOption]
    * }}}
    *
    * This type provides a convenient (maybe?) way to inline
    * the above type:
    * {{{
    * method[(List ∘ Option)]
    * }}}
    *
    * Note: This alias is equivalent to:
    * {{{
    * method[α =>> List[Option[α]]]
    * }}}
    */
  type ∘[F[_], G[_]] = [X] =>> F[G[X]]
}

sealed trait AttrSyntax {
  extension [F[_], A](lower: F[A]) {
    def attr[G[a] >: F[a], B](ask: B): AttrF[G, B, A] =
      AttrF(ask, lower)
  }
}

sealed trait LiftSyntax {
  extension [A, B](f: A => B) {
    def lift[F[_]](using F: Applicative[F]): A => F[B] = a => F.pure(f(a))
  }
}

sealed trait FixSyntax {
  extension [F[_], A](unfix: F[A]) {
    def fix[G[A] >: F[A]](using ev: A =:= Fix[G]): Fix[G] =
      Fix(unfix.asInstanceOf[G[Fix[G]]])
  }
}

sealed trait UnfixSyntax {
  extension [F[_]](fix: Fix[F]) {
    def unfix: F[Fix[F]] = Fix.un(fix)
  }
}

sealed trait EmbedSyntax {
  extension [F[_], T](t: F[T])(using E: Embed[F, T]) {
    def embed: T = E.algebra(t)
  }
}

sealed trait ProjectSyntax {
  extension [F[_], T](t: T)(using PFT: Project[F, T]) {
    def project: F[T] = PFT.coalgebra(t)
  }

  extension [F[_], T](t: T)(using PFT: Project[F, T], FF: Foldable[F]) {
    def all(p: T => Boolean): Boolean =
      Project.all(t)(p)

    def any(p: T => Boolean): Boolean =
      Project.any(t)(p)

    def collect[U: Monoid, B](pf: PartialFunction[T, B])(
        implicit U: Basis[ListF[B, *], U]): U =
      Project.collect[F, T, U, B](t)(pf)

    def contains(c: T)(implicit T: Eq[T]): Boolean =
      Project.contains(t, c)

    def foldMap[Z: Monoid](f: T => Z): Z =
      Project.foldMap(t)(f)

    def foldMapM[M[_], Z](
        f: T => M[Z])(implicit M: Monad[M], Z: Monoid[Z]): M[Z] =
      Project.foldMapM(t)(f)
  }
}
