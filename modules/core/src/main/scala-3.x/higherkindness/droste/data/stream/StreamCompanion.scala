package higherkindness.droste.data.stream

trait StreamCompanion {
  object implicits {
    extension [A](fa: Stream[A]) {
      def map[B](f: A => B): Stream[B]             = Stream.map(fa)(f)
      def flatMap[B](f: A => Stream[B]): Stream[B] = Stream.flatMap(fa)(f)
      def take(n: Int): Stream[A]                  = Stream.take(fa)(n)
      def toList: List[A]                          = Stream.toList(fa)
    }
  }
}
