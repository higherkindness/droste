package qq.droste
package tests

import org.scalacheck.Properties
import org.scalacheck.Prop._

import qq.droste.data.stream._

final class StreamTests extends Properties("StreamTests") {

  import Stream.implicits._

  property("Stream.map") =
    forAll((f: Int => String) =>
      Stream.naturalNumbers.map(f).take(100).toList ?= List.iterate(1, 100)(_ + 1).map(f))

  property("Stream.flatMap") = {
    val res = Stream.naturalNumbers.flatMap(n => Stream.naturalNumbers.take(n)).take(5000).toList
    res ?= List.iterate(1, 100)(_ + 1).flatMap(n => List.iterate(1, n)(_ + 1)).take(5000)
  }


}
