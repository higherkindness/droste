package qq.droste
package scalacheck

import data._

import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.`!droste!DROSTE!droste!`.Compat

import cats.Applicative
import cats.Monad
import cats.MonoidK
import cats.Traverse
import cats.implicits._

object `package` {

  private[this] def genSizedF[F[_]: Applicative: MonoidK](size: Int): Gen[F[Int]] =
    Gen.choose(0, size).map(n => if (n > 0) n.pure[F] else MonoidK[F].empty[Int])

  private[droste] implicit val drosteCatsMonadForScalacheckGen: Monad[Gen] = new Monad[Gen] {
    def pure[A](a: A): Gen[A] = Gen.const(a)
    override def map[A, B](a: Gen[A])(f: A => B): Gen[B] = a.map(f)
    def flatMap[A, B](a: Gen[A])(f: A => Gen[B]): Gen[B] = a.flatMap(f)
    def tailRecM[A, B](a: A)(f: A => Gen[Either[A, B]]): Gen[B] =
      Compat.gen_tailRecM(a)(f)
  }

  def drosteGenAttr[F[_]: Applicative: MonoidK, A: Arbitrary](
    implicit ev: Traverse[AttrF[F, A, ?]]
  ): Gen[Attr[F, A]] =
    Gen.sized(maxSize =>
      scheme.anaM(CoalgebraM((size: Int) =>
        for {
          a <- arbitrary[A]
          f <- genSizedF[F](size)
        } yield AttrF(a, f)
      )).apply(maxSize))

  def drosteGenAttrF[F[_], A, B](implicit ev: Arbitrary[(A, F[B])]): Gen[AttrF[F, A, B]] =
    ev.arbitrary.map(AttrF.apply(_))

  def drosteGenFix[F[_]: Applicative: Traverse: MonoidK]: Gen[Fix[F]] =
    Gen.sized(maxSize =>
      scheme[Fix].anaM(CoalgebraM(genSizedF[F])).apply(maxSize))

  def drosteGenMu[F[_]: Applicative: Traverse: MonoidK](
    implicit ev: Embed[F, Mu[F]]
  ): Gen[Mu[F]] =
    Gen.sized(maxSize =>
      scheme[Mu].anaM(CoalgebraM(genSizedF[F])).apply(maxSize))

  def drosteGenNu[F[_]: Applicative: Traverse: MonoidK](
    implicit ev: Embed[F, Nu[F]]
  ): Gen[Nu[F]] =
    Gen.sized(maxSize =>
      scheme[Nu].anaM(CoalgebraM(genSizedF[F])).apply(maxSize))

  implicit def drosteArbitraryAttr[F[_]: Applicative: MonoidK, A: Arbitrary](
    implicit ev: Traverse[AttrF[F, A, ?]]
  ): Arbitrary[Attr[F, A]] =
    Arbitrary(drosteGenAttr)

  implicit def drosteArbitraryAttrF[F[_], A, B](
    implicit ev: Arbitrary[(A, F[B])]
  ): Arbitrary[AttrF[F, A, B]] =
    Arbitrary(drosteGenAttrF)

  implicit def drosteArbitraryFix[F[_]: Applicative: Traverse: MonoidK]: Arbitrary[Fix[F]] =
    Arbitrary(drosteGenFix)

  implicit def drosteArbitraryMu[F[_]: Applicative: Traverse: MonoidK](
    implicit ev: Embed[F, Mu[F]]
  ): Arbitrary[Mu[F]] =
    Arbitrary(drosteGenMu)

  implicit def drosteArbitraryNu[F[_]: Applicative: Traverse: MonoidK](
    implicit ev: Embed[F, Nu[F]]
  ): Arbitrary[Nu[F]] =
    Arbitrary(drosteGenNu)

}
