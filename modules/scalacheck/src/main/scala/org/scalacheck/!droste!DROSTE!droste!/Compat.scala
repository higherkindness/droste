package org.scalacheck
// note: an obnoxious package is used so that nobody will be inclined
// to casually import this in a downstream project
package `!droste!DROSTE!droste!`

import rng.Seed
import Gen._

import scala.annotation.tailrec

object Compat {
  private type P = Parameters

  // cargo culted from future versions of Scalacheck to avoid
  // current binary dep errors
  def gen_tailRecM[A, B](a0: A)(fn: A => Gen[Either[A, B]]): Gen[B] = {
    @tailrec
    def tailRecMR(a: A, seed: Seed, labs: Set[String])(fn: (A, Seed) => R[Either[A, B]]): R[B] = {
      val re = fn(a, seed)
      val nextLabs = labs | re.labels
      re.retrieve match {
        case None => r(None, re.seed).copy(l = nextLabs)
        case Some(Right(b)) => r(Some(b), re.seed).copy(l = nextLabs)
        case Some(Left(a)) => tailRecMR(a, re.seed, nextLabs)(fn)
      }
    }
    gen[B] { (p: P, seed: Seed) =>
      tailRecMR(a0, seed, Set.empty) { (a, seed) => fn(a).doApply(p, seed) }
    }
  }
}
