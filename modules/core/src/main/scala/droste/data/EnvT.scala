package droste
package data

final case class EnvT[F[_], E, A](ask: E, lower: F[A])
