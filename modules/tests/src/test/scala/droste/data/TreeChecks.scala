package droste
package data

import tree._

import cats.implicits._

import org.scalacheck._
import org.scalacheck.Prop._

class Treechecks extends Properties("Tree") {

  property("fixed.render example") = {

    val t0: Tree[String] =
      Node("root",
        Node("A",
          Leaf("value0"),
          Leaf("value1"),
          Leaf("value2")),
        Node("B",
          Leaf("value0"),
          Leaf("value1"),
          Leaf("value2"),
          Leaf("value3")),
        Node("C",
          Leaf("value0")))

    val expected =
      """
      |root
      | ├─ A
      | │  ├─ value0
      | │  ├─ value1
      | │  └─ value2
      | ├─ B
      | │  ├─ value0
      | │  ├─ value1
      | │  ├─ value2
      | │  └─ value3
      | └─ C
      |    └─ value0""".stripMargin.trim

    TreeF.render(t0.fixed) ?= expected
  }

}
