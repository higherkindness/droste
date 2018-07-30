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
    * {{{type Cofree[F[_], A] = (A, F[Cofree[F, A]])}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type Cofree[F[_], A] // = (A, F[Cofree[F, A]])

  type :<[F[_], A] = Cofree[F, A]
  val  :<          = Cofree

  /** The pattern functor for [[Cofree]].
    *
    * More commonly known as the seldom used environment
    * comonad transformer.
    *
    * Implemented as an obscured alias:
    * {{{type EnvT[E, W[_], A] = (E, W[A])}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type EnvT[E, W[_], A] // = (E, W[A])

  /** The pattern functor for [[Free]].
    *
    * The dual of [[EnvT]].
    *
    * Implemented as an obscured alias:
    * {{{type CoenvT[E, W[_], A] = Either[E, W[A]]}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type CoenvT[E, W[_], A] // = Either[E, W[A]]

  /** A very basic free monad.
    *
    * This implementation is not lazy and is used strictly for
    * data.
    *
    * Implemented as an obscured alias:
    * {{{type Free[F[_], A] = Either[A, F[Free[F, A]]]}}}
    *
    * The companion object can be used to translate between
    * representations.
    */
  type Free[F[_], A] // = Either[A, F[Free[F, A]]]
}
