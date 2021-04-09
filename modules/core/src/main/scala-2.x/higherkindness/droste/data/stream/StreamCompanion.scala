package higherkindness.droste.data.stream

trait StreamCompanion {
  final class StreamOps[A](val fa: Stream[A]) extends AnyVal {
    def map[B](f: A => B): Stream[B]             = Stream.map(fa)(f)
    def flatMap[B](f: A => Stream[B]): Stream[B] = Stream.flatMap(fa)(f)
    def take(n: Int): Stream[A]                  = Stream.take(fa)(n)
    def toList: List[A]                          = Stream.toList(fa)
  }

  object implicits {
    implicit def toStreamOps[A](fa: Stream[A]): StreamOps[A] =
      new StreamOps[A](fa)
  }
}
