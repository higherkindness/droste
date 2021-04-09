package higherkindness.droste.macros

import scala.deriving.*
import scala.compiletime.*

import cats.{Applicative, Traverse}
import cats.syntax.all._

import higherkindness.droste.util.DefaultTraverse

import MacroUtils.*

// Inspired and made possible by https://github.com/cb372/type-class-derivation-in-scala-3
object DeriveTraverse {
  trait TraverseFor[A] {
    type TC[_]

    def instance: Traverse[TC]

    def mkValueMapper: [G[_], A, B] => (TC[A]) => (A => G[B]) => Applicative[G] ?=> G[TC[B]] =
      [G[_], A, B] => (x: TC[A]) => (fn: A => G[B]) => (G: Applicative[G]) ?=> instance.traverse[G, A, B](x)(fn)
  }

  transparent inline implicit def traverseFor[F[_], T]: TraverseFor[F[T]] =
    summonFrom {
      case t: Traverse[F] =>
        inline erasedValue[T] match {
          case _: Dummy =>
            new TraverseFor[F[T]] {
              type TC[X] = F[X]
              val instance: Traverse[F] = t
            }
          case _ =>
            summonFrom {
              case t1: TraverseFor[T] =>
                new TraverseFor[F[T]] {
                  type TC[X] = F[t1.TC[X]]
                  val instance: Traverse[[X] =>> F[t1.TC[X]]] = t.compose[t1.TC](using t1.instance)
                }
            }
        }
    }

  transparent inline def valueMapper[T]: Any =
    summonFrom {
      case t: TraverseFor[T] =>
        t.mkValueMapper
      case _ =>
        inline erasedValue[T] match {
          case _: Dummy =>
            [G[_], A, B] => (x: A) => (fn: A => G[B]) => (G: Applicative[G]) ?=> fn(x)
          case _ =>
            [G[_], A, B] => (x: T) => (fn: A => G[B]) => (G: Applicative[G]) ?=> G.pure(x)
        }
    }

  inline def productMapper[Instances <: Tuple]: Array[Any] = {
    val arr = new Array[Any](constValue[Tuple.Size[Instances]])
    productMapper0[Instances](arr, 0)
  }

  inline def productMapper0[Instances <: Tuple](arr: Array[Any], idx: Int): Array[Any] =
    inline erasedValue[Instances] match {
      case _: EmptyTuple =>
        arr
      case _: (a *: b) =>
        arr(idx) = valueMapper[a]
        productMapper0[b](arr, idx + 1)
    }

  inline def mapProduct[G[_], A, B](elems: Product, mappers: Array[Any], fn: A => G[B])(using G: Applicative[G]): G[Tuple] = {
    var i: Int = elems.productArity - 1
    var res: G[Tuple] = G.pure(EmptyTuple)
    while (i >= 0) {
      val mapper = mappers(i).asInstanceOf[Any => (A => G[B]) => Applicative[G] ?=> G[Any]]
      val elem = elems.productElement(i)
      res = G.map2(mapper(elem)(fn), res)(_ *: _)
      i -= 1
    }

    res
  }

  transparent inline def traverseChildren[Children <: Tuple]: Tuple =
    inline erasedValue[Children] match {
      case _: EmptyTuple => EmptyTuple
      case _: (a *: b) =>
        summonFrom {
          case app: Apply[`a`] => traverseProduct[app.F](summonInline[ProductMirrorOf[app.F]]) *: traverseChildren[b]
        }
    }

  inline def traverseSum[F[_]](p: CoproductMirrorOf[F]): Traverse[F] = {
    val children = traverseChildren[p.MirroredElemTypes[Dummy]].toArray

    new DefaultTraverse[F] {
      def traverse[G[_], A, B](fa: F[A])(fn: A => G[B])(using G: Applicative[G]): G[F[B]] = {
        val i = p.ordinal(fa.asInstanceOf)
        children(i).asInstanceOf[Traverse[F]].traverse(fa)(fn)
      }
    }
  }

  inline def traverseProduct[F[_]](s: ProductMirrorOf[F]): Traverse[F] = {
    val mappers = productMapper[s.MirroredElemTypes[Dummy]]

    new DefaultTraverse[F] {
      def traverse[G[_], A, B](fa: F[A])(fn: A => G[B])(using G: Applicative[G]): G[F[B]] = {
        val n = mappers.size
        if (n == 0) G.pure(fa.asInstanceOf[F[B]])
        else mapProduct(fa.asInstanceOf[Product], mappers, fn).map(s.fromProduct(_).asInstanceOf[F[B]])
      }
    }
  }
}
