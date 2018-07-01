package qq.droste
package syntax

import cats.Applicative

object `package` {

  /** Compose two functors `F` and `G.
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

  implicit final class LiftArrowOps[A, B](val f: A => B) extends AnyVal {
    def lift[F[_]](implicit F: Applicative[F]): A => F[B] = a => F.pure(f(a))
  }
}
