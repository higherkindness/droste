package higherkindness.droste.derivation

import cats.{Applicative, Eval, Traverse}
import shapeless3.deriving.{Const, Continue, K1}
import scala.compiletime.*

/*
Credit to the `kittens` project. This is a direct copy/paste from there

https://github.com/typelevel/kittens/blob/5b3b7ca168b9636f340b23bc95a4fd1506a26707/core/src/main/scala-3/cats/derived/traverse.scala
*/


type DerivedTraverse[F[_]] = Derived[Traverse[F]]
object DerivedTraverse:
  type Or[F[_]] = Derived.Or[Traverse[F]]
  inline def apply[F[_]]: Traverse[F] =
    import DerivedTraverse.given
    summonInline[DerivedTraverse[F]].instance

  given [T]: DerivedTraverse[Const[T]] = new Traverse[Const[T]]:
    override def map[A, B](fa: T)(f: A => B): T = fa
    def foldLeft[A, B](fa: T, b: B)(f: (B, A) => B): B = b
    def foldRight[A, B](fa: T, lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = lb
    def traverse[G[_], A, B](fa: T)(f: A => G[B])(using G: Applicative[G]): G[T] = G.pure(fa)

  given [F[_], G[_]](using F: Or[F], G: Or[G]): DerivedTraverse[[x] =>> F[G[x]]] =
    F.unify `compose` G.unify

  given [F[_]](using inst: K1.ProductInstances[Or, F]): DerivedTraverse[F] =
    given K1.ProductInstances[Traverse, F] = inst.unify
    new Product[Traverse, F] {}

  given [F[_]](using inst: => K1.CoproductInstances[Or, F]): DerivedTraverse[F] =
    given K1.CoproductInstances[Traverse, F] = inst.unify
    new Coproduct[Traverse, F] {}

  trait Product[T[x[_]] <: Traverse[x], F[_]](using inst: K1.ProductInstances[T, F])
      extends DerivedFunctor.Generic[T, F], DerivedFoldable.Product[T, F], Traverse[F]:

    def traverse[G[_], A, B](fa: F[A])(f: A => G[B])(using G: Applicative[G]): G[F[B]] =
      inst.traverse[A, G, B](fa) { [a, b] => (ga: G[a], f: a => b) =>
        G.map(ga)(f)
      } { [a] => (x: a) =>
        G.pure(x)
      } { [a, b] => (gf: G[a => b], ga: G[a]) =>
        G.ap(gf)(ga)
      } { [f[_]] => (tf: T[f], fa: f[A]) =>
        tf.traverse(fa)(f)
      }

  trait Coproduct[T[x[_]] <: Traverse[x], F[_]](using inst: K1.CoproductInstances[T, F])
      extends DerivedFunctor.Generic[T, F], DerivedFoldable.Coproduct[T, F], Traverse[F]:

    def traverse[G[_], A, B](fa: F[A])(f: A => G[B])(using G: Applicative[G]): G[F[B]] =
      inst.traverse[A, G, B](fa) { [a, b] => (ga: G[a], f: a => b) =>
        G.map(ga)(f)
      } { [a] => (x: a) =>
        G.pure(x)
      } { [a, b] => (gf: G[a => b], ga: G[a]) =>
        G.ap(gf)(ga)
      } { [f[_]] => (tf: T[f], fa: f[A]) =>
        tf.traverse(fa)(f)
      }
