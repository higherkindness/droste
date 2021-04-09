package higherkindness.droste.macros

import cats.Traverse

import MacroUtils.*

inline def deriveTraverse[F[_]](using m: MirrorOf[F]): Traverse[F] =
  inline m match {
    case p: ProductMirrorOf[F] => DeriveTraverse.traverseProduct(p)
    case s: CoproductMirrorOf[F] => DeriveTraverse.traverseSum(s)
  }