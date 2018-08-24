package qq.droste
package syntax

import cats.Applicative

import data.AttrF
import data.Fix

object all
    extends AliasSyntax
    with AttrSyntax
    with LiftSyntax
    with FixSyntax
    with UnfixSyntax
    with EmbedSyntax
    with ProjectSyntax

object alias extends AliasSyntax
object attr extends AttrSyntax
object lift extends LiftSyntax
object fix extends FixSyntax
object unfix extends UnfixSyntax
object embed extends EmbedSyntax
object project extends ProjectSyntax

sealed trait AliasSyntax {
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
    * method[(List ∘ Option)#λ]
    * }}}
    *
    * Note: If you have the kind projector plugin enabled, this alias
    * is equivalent to:
    * {{{
    * method[λ[α => List[Option[α]]]]
    * }}}
    */
  type ∘[F[_], G[_]] = { type λ[α] = F[G[α]] }

  type &[L, R] = (L, R)
  type |[L, R] = Either[L, R]
}

sealed trait AttrSyntax {
  implicit def toAttrySyntaxOps[V[_], A](lower: V[A]): AttrSyntax.Ops[V, A] =
    new AttrSyntax.Ops(lower)
}

object AttrSyntax {
  final class Ops[F[_], B](val lower: F[B]) extends AnyVal {
    implicit def attr[G[a] >: F[a], A](ask: A): AttrF[G, A, B] = AttrF(ask, lower)
  }
}

sealed trait LiftSyntax {
  implicit def toLiftSyntaxOps[A, B](f: A => B): LiftSyntax.Ops[A, B] =
    new LiftSyntax.Ops(f)
}

object LiftSyntax {
  final class Ops[A, B](val f: A => B) extends AnyVal {
    def lift[F[_]](implicit F: Applicative[F]): A => F[B] = a => F.pure(f(a))
  }
}

sealed trait FixSyntax {
  implicit def toFixSyntaxOps[F[_]](unfix: F[_]): FixSyntax.Ops[F] =
    new FixSyntax.Ops(unfix)
}

object FixSyntax {
  final class Ops[F[_]](val unfix: F[_]) extends AnyVal {
    def fix[G[a] >: F[a]]: Fix[G] = Fix(unfix.asInstanceOf[G[Fix[G]]])
  }
}

sealed trait UnfixSyntax {
  implicit def toUnfixSyntaxOps[F[_]](fix: Fix[F]): UnfixSyntax.Ops[F] =
    new UnfixSyntax.Ops(fix)
}

object UnfixSyntax {
  final class Ops[F[_]](val fix: Fix[F]) extends AnyVal {
    def unfix: F[Fix[F]] = Fix.un(fix)
  }
}

sealed trait EmbedSyntax {
  implicit def toEmbedSyntaxOps[F[_], T](t: F[T])(implicit Embed: Embed[F, T]): EmbedSyntax.Ops[F, T] =
    new EmbedSyntax.Ops[F, T] {
      def tc   = Embed
      def self = t
    }
}

object EmbedSyntax {
  trait Ops[F[_], T] {
    def tc: Embed[F, T]
    def self: F[T]

    def embed: T = tc.algebra(self)
  }
}

sealed trait ProjectSyntax {
  implicit def toProjectSyntaxOps[F[_], T](t: T)(implicit Project: Project[F, T]): ProjectSyntax.Ops[F, T] =
    new ProjectSyntax.Ops[F, T] {
      def tc   = Project
      def self = t
    }
}

object ProjectSyntax {
  trait Ops[F[_], T] {
    def tc: Project[F, T]
    def self: T

    def project: F[T] = tc.coalgebra(self)
  }
}
