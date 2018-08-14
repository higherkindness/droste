package qq.droste
package tests

import org.scalacheck.Gen
import org.scalacheck.droste.Compat
import cats.Monad

private[tests] trait ScalacheckSupport {

  implicit val drosteCatsMonadForScalacheckGen: Monad[Gen] = new Monad[Gen] {
    def pure[A](a: A): Gen[A] = Gen.const(a)
    override def map[A, B](a: Gen[A])(f: A => B): Gen[B] = a.map(f)
    def flatMap[A, B](a: Gen[A])(f: A => Gen[B]): Gen[B] = a.flatMap(f)
    def tailRecM[A, B](a: A)(f: A => Gen[Either[A, B]]): Gen[B] =
      Compat.gen_tailRecM(a)(f)
  }

}
