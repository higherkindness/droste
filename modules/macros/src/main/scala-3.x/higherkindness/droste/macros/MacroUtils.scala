package higherkindness.droste.macros

import scala.deriving.*

object MacroUtils {
  type PolyMirror[C, O[_]] = C { type MirroredType[X] = O[X]; type MirroredElemTypes[X] <: Tuple }
  type MirrorOf[O[_]] = PolyMirror[Mirror, O]
  type ProductMirrorOf[O[_]] = PolyMirror[Mirror.Product, O]
  type CoproductMirrorOf[O[_]] = PolyMirror[Mirror.Sum, O]

  final abstract class Dummy

  trait Apply[T] {
    type F[_]
  }

  object Apply {
    type Aux[T, F0[_]] = Apply[T] { type F[X] = F0[X] }

    transparent inline implicit def instance[F[_], T]: Apply.Aux[F[T], F] = null.asInstanceOf[Apply.Aux[F[T], F]]
  }
}