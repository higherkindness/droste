package qq.droste.macros

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly

import impl.deriveFixedPointMacro

@compileTimeOnly("enable macro paradise to expand macro annotations")
class deriveFixedPoint extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro deriveFixedPointMacro.impl
}
