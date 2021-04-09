package higherkindness.droste.data

import cats.Functor

import higherkindness.droste.Basis
import higherkindness.droste.meta.Meta

trait AttrCompanion {
  inline def apply[F[_], A](inline f: (A, F[Attr[F, A]])): Attr[F, A] = Meta.fastCast[(A, F[Attr[F, A]]), Attr[F, A]](f)
  inline def un[F[_], A](inline f: Attr[F, A]): (A, F[Attr[F, A]]) = Meta.fastCast[Attr[F, A], (A, F[Attr[F, A]])](f)
}

trait AttrFCompanion {
  inline def apply[F[_], A, B](inline f: (A, F[B])): AttrF[F, A, B] = Meta.fastCast[(A, F[B]), AttrF[F, A, B]](f)
  inline def un[F[_], A, B](inline f: AttrF[F, A, B]): (A, F[B]) = Meta.fastCast[AttrF[F, A, B], (A, F[B])](f)
}

trait CoattrCompanion {
  inline def apply[F[_], A](inline f: Either[A, F[Coattr[F, A]]]): Coattr[F, A] = Meta.fastCast[Either[A, F[Coattr[F, A]]], Coattr[F, A]](f)
  inline def un[F[_], A](inline f: Coattr[F, A]): Either[A, F[Coattr[F, A]]] = Meta.fastCast[Coattr[F, A], Either[A, F[Coattr[F, A]]]](f)
}

trait CoattrFCompanion {
  inline def apply[F[_], A, B](inline f: Either[A, F[B]]): CoattrF[F, A, B] = Meta.fastCast[Either[A, F[B]], CoattrF[F, A, B]](f)
  inline def un[F[_], A, B](inline f: CoattrF[F, A, B]): Either[A, F[B]] = Meta.fastCast[CoattrF[F, A, B], Either[A, F[B]]](f)
}

trait FixCompanion {
  inline def apply[F[_]](inline f: F[Fix[F]]): Fix[F] = Meta.fastCast[F[Fix[F]], Fix[F]](f)
  inline def un[F[_]](inline f: Fix[F]): F[Fix[F]] = Meta.fastCast[Fix[F], F[Fix[F]]](f)
}
