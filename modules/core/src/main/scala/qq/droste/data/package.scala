package qq.droste

package object data {

  /** A fix point function for types.
    *
    * Implemented as an obscured alias:
    * {{{type Fix[F[_]] = F[Fix[F]]}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type Fix[F[_]] // = F[Fix[F]]

  /** A very basic cofree comonad.
    *
    * Implemented as an obscured alias:
    * {{{type Attr[F[_], A] = (A, F[Attr[F, A]])}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type Attr[F[_], A] // = (A, F[Attr[F, A]])

  type :<[F[_], A] = Attr[F, A]
  val  :<          = Attr

  /** The pattern functor for [[Attr]].
    *
    * This is also the environment comonad transformer "EnvT".
    *
    * Implemented as an obscured alias:
    * {{{type AttrF[F[_], A, B] = (A, F[B])}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type AttrF[F[_], A, B] // = (A, F[B])

  /** A very basic free monad.
    *
    * The dual of [[Attr]].
    *
    * This implementation is not lazy and is used strictly for
    * data.
    *
    * Implemented as an obscured alias:
    * {{{type Coattr[F[_], A] = Either[A, F[Coattr[F, A]]]}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type Coattr[F[_], A] // = Either[A, F[Coattr[F, A]]]

  /** The pattern functor for [[Coattr]].
    *
    * The dual of [[AttrF]].
    *
    * Implemented as an obscured alias:
    * {{{type CoattrF[F[_], A, B] = Either[A, F[B]]}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type CoattrF[F[_], A, B] // = Either[A, F[B]]
}
