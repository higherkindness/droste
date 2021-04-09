package higherkindness.droste.data

import meta.Meta

trait AttrCompanion {
  def apply[F[_], A](f: (A, F[Attr[F, A]])): Attr[F, A] = macro Meta.fastCast
  def un[F[_], A](f: Attr[F, A]): (A, F[Attr[F, A]]) = macro Meta.fastCast
}

trait AttrFCompanion {
  def apply[F[_], A, B](f: (A, F[B])): AttrF[F, A, B] = macro Meta.fastCast
  def un[F[_], A, B](f: AttrF[F, A, B]): (A, F[B]) = macro Meta.fastCast
}

trait CoattrCompanion {
  def apply[F[_], A](f: Either[A, F[Coattr[F, A]]]): Coattr[F, A] =
    macro Meta.fastCast
  def un[F[_], A](f: Coattr[F, A]): Either[A, F[Coattr[F, A]]] =
    macro Meta.fastCast
}

trait CoattrFCompanion {
  def apply[F[_], A, B](f: Either[A, F[B]]): CoattrF[F, A, B] =
    macro Meta.fastCast
  def un[F[_], A, B](f: CoattrF[F, A, B]): Either[A, F[B]] = macro Meta.fastCast
}

trait FixCompanion {
  def apply[F[_]](f: F[Fix[F]]): Fix[F] = macro Meta.fastCast
  def un[F[_]](f: Fix[F]): F[Fix[F]] = macro Meta.fastCast
}
