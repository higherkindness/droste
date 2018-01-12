package droste
package typeclass

import cats.~>

trait FunctorK[M[_[_], _]] {
  def mapK[F[_], G[_], A](mf: M[F, A])(f: F ~> G): M[G, A]
}

object FunctorK {

  trait Ops[M[_[_], _], F[_], A] {
    def typeClassInstance: FunctorK[M]
    def self: M[F, A]
    def mapK[G[_]](f: F ~> G): M[G, A] = typeClassInstance.mapK(self)(f)
  }

  trait ToFunctorKOps {
    implicit def toFunctorKOps[M[_[_], _], F[_], A](mf: M[F, A])(implicit tc: FunctorK[M]): Ops[M, F, A] =
      new Ops[M, F, A] {
        val typeClassInstance = tc
        val self              = mf
      }
  }

}
