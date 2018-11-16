package qq.droste
package data
package stream

import cats.syntax.applicative._
import cats.syntax.functor._

import java.util.{ Iterator => JavaIterator }
import scala.annotation.tailrec

import list._

object `package` {
  type Stream[A] = Nu[ListF[A, ?]]
}

object Stream {

  def map[A, B](fa: Stream[A])(f: A => B): Stream[B] =
    Nu(Coalgebra(fa.unfold.run andThen (_ match {
      case ConsF(head, tail) => ConsF(f(head), tail)
      case NilF => NilF
    })), fa.a)

  def flatten[A](fa: Stream[Stream[A]]): Stream[A] =
    flatMap(fa)(identity)

  def flatMap[A, B](fa: Stream[A])(f: A => Stream[B]): Stream[B] = {
    type S = Either[fa.A, (Stream[B], fa.A)]

    lazy val inner: fa.A => ListF[B, S] =
      fa.unfold(_) match {
        case ConsF(h, t) => Nu.un(f(h)) match {
          case ConsF(hh, tt) => ConsF(hh, Right((tt, t)))
          case NilF => inner(t)
        }
        case NilF => NilF
      }

    val outer: S => ListF[B, S] = _ match {
      case Left(seed) => inner(seed)
      case Right(cont) => Nu.un(cont._1) match {
        case ConsF(h, t) => ConsF(h, Right((t, cont._2)))
        case NilF => inner(cont._2)
      }
    }

    Nu(Coalgebra(outer), Left(fa.a))
  }

  object coalgebras {
    def increment: Coalgebra[ListF[Int, ?], Int] =
      Coalgebra(n => ConsF(n, n + 1))
  }

  def naturalNumbers: Stream[Int] =
    Nu(coalgebras.increment, 1)

  def take[A](fa: Stream[A])(n: Int): Stream[A] =
    Nu(Coalgebra[ListF[A, ?], (Int, fa.A)](ia =>
      if (ia._1 <= 0) NilF else fa.unfold(ia._2) match {
        case ConsF(head, tail) => ConsF(head, (ia._1 - 1, tail))
        case NilF => NilF
      }
    ), (n, fa.a))

  def fromJavaIterator[A](it0: => JavaIterator[A]): Stream[A] =
    Nu(Coalgebra((it: JavaIterator[A]) => if (it.hasNext) ConsF(it.next(), it) else NilF), it0)

  def fromIterator[A](it0: => Iterator[A]): Stream[A] =
    Nu(Coalgebra((it: Iterator[A]) => if (it.hasNext) ConsF(it.next(), it) else NilF), it0)

  def foldLeft[A, B](fa: Stream[A])(z: B)(f: (B, A) => B): B = {
    @tailrec def kernel(in: Stream[A], out: B): B = Nu.un(in) match {
      case ConsF(head, tail) => kernel(tail, f(out, head))
      case NilF              => out
    }
    kernel(fa, z)
  }

  def toList[A](fa: Stream[A]): List[A] =
    foldLeft(fa)(List.empty[A])((acc, a) => a :: acc).reverse

  final class StreamOps[A](val fa: Stream[A]) extends AnyVal {
    def map[B](f: A => B): Stream[B] = Stream.map(fa)(f)
    def flatMap[B](f: A => Stream[B]): Stream[B] = Stream.flatMap(fa)(f)
    def take(n: Int): Stream[A] = Stream.take(fa)(n)
    def toList: List[A] = Stream.toList(fa)
  }

  object implicits {
    implicit def toStreamOps[A](fa: Stream[A]): StreamOps[A] = new StreamOps[A](fa)
  }

}
