package qq.droste.macros.impl

import scala.reflect.macros.blackbox

object deriveFixedPointMacro {
  def impl(c: blackbox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import Flag._
    val inputs = annottees.map(_.tree).toList

    require(inputs.length == 2, "@deriveFixedPoint should annotate a sealed [trait|abstract class] with companion")

    val clait: ClassDef      = inputs.collect({ case c: ClassDef  => c }).head
    val companion: ModuleDef = inputs.collect({ case c: ModuleDef => c }).head

    val A: TypeName                     = c.freshName(TypeName("A"))
    val claitTypeParams                 = clait.tparams
    val claitTypeParamNames             = claitTypeParams.map(_.name)
    val claitTypeParamNamesAsTrees      = claitTypeParams.map(x => Ident(x.name))
    val claitTypeParamNamesWithA        = claitTypeParamNames :+ A
    val claitTypeParamNamesWithAAsTrees = claitTypeParamNamesWithA.map(x => Ident(x))

    val NonRecursiveAdtName: TypeName = TypeName(s"${clait.name}F")
    val NonRecursiveAdtFullName: AppliedTypeTree =
      AppliedTypeTree(Ident(NonRecursiveAdtName), claitTypeParamNamesWithAAsTrees)

    def isSealed(classOrTrait: ClassDef): Boolean =
      classOrTrait.mods.hasFlag(TRAIT) ||
    classOrTrait.mods.hasFlag(ABSTRACT) &&
    classOrTrait.mods.hasFlag(SEALED)

    val isCase: PartialFunction[Tree, Tree] = {
      case c: ClassDef if c.mods.hasFlag(CASE)  => c
      case c: ModuleDef if c.mods.hasFlag(CASE) => c
    }

    val AdtCases = companion.impl.body.collect(isCase)

    def substType(rec: Tree => Boolean): Tree => Tree = {
      case x: Ident if rec(x) => Ident(A)
      case x: AppliedTypeTree => AppliedTypeTree(x.tpt, x.args.map(substType(rec)))
      case x => x
    }

    val convertCaseClassParam: ValDef => ValDef = { valDef =>
      val recursive: Tree => Boolean = _.toString == clait.name.toString

      ValDef(valDef.mods, valDef.name, substType(recursive)(valDef.tpt), valDef.rhs)
    }

    def convertCaseObject(module: ModuleDef): ClassDef = {
      val Name: TypeName = TypeName(s"${module.name}F")

      q"case class $Name[..${clait.tparams}, $A]() extends $NonRecursiveAdtFullName"
    }

    def getCaseClassParams(caseClass: ClassDef): List[ValDef] =
      caseClass.impl.body
        .collect({
          case v: ValDef if v.mods.hasFlag(PARAMACCESSOR) && v.mods.hasFlag(CASEACCESSOR) => v
        })
        .map(convertCaseClassParam)

    def convertCaseClass(caseClass: ClassDef): ClassDef = {
      val Name: TypeName = TypeName(s"${caseClass.name}F")

      val params: List[ValDef] = getCaseClassParams(caseClass)

      q"case class $Name[..${clait.tparams}, $A](..$params) extends $NonRecursiveAdtFullName"
    }

    val NonRecursiveAdtCases: List[ClassDef] =
      AdtCases.map {
        case c: ModuleDef => convertCaseObject(c)
        case c: ClassDef  => convertCaseClass(c)
        case _            => sys.error("Nope")
      }

    val nonRecursiveAdt: ClassDef =
      q"""sealed trait $NonRecursiveAdtName[..${clait.tparams}, $A] extends Product with Serializable"""

    val λ: TypeDef = q"type λ[α] = $NonRecursiveAdtName[..${clait.tparams.map(_.name)}, α]"

    val traverseInstance: DefDef = {

      val G = c.freshName(TypeName("G"))
      val AA = c.freshName(TypeName("AA"))
      val B = c.freshName(TypeName("B"))

      val cases = NonRecursiveAdtCases map { origin =>
        val name = TermName(origin.name.toString)
        val params = getCaseClassParams(origin)
        val arity = params.length
        val freshTerms = List.fill(arity)(TermName(c.freshName))
        val binds = freshTerms.map(x => Bind(x, Ident(termNames.WILDCARD)))
        val args = params.zip(freshTerms).map { case (valDef, t) =>
          if (valDef.tpt.toString == A.toString) {
            q"fn($t)"
          } else if(valDef.tpt.toString.contains(A.toString)) {
            val T = valDef.tpt.asInstanceOf[AppliedTypeTree].tpt
            q"cats.Traverse[$T].traverse($t)(fn)"
          } else {
            q"cats.Applicative[$G].pure($t)"
          }
        }
        val body = if (arity > 1) {
          val mapN: TermName = TermName(s"map${arity.toString}")
          q"cats.Applicative[$G].$mapN(..$args)($name.apply[$B])"
        } else if(arity == 1) {
          q"cats.Applicative[$G].map($args.head)($name.apply[$B])"
        } else {
          q"cats.Applicative[$G].pure($name[$B]())"
        }

        cq"$name(..$binds) => $body"
      }

      val mtch = Match(Ident(TermName("fa")), cases)

      q"""
      implicit def traverseInstance[..${clait.tparams}]: cats.Traverse[λ] = new qq.droste.util.DefaultTraverse[λ] {
        import cats.implicits._

        def traverse[$G[_]: cats.Applicative, $AA, $B](fa: λ[$AA])(fn: $AA => $G[$B]): $G[λ[$B]] = $mtch
      }
      """
    }


    val toRecursive: DefDef = {
      val embedAlgebraCases: List[CaseDef] =
        (NonRecursiveAdtCases zip AdtCases) map {
          case (origin, target: ClassDef) =>
            val originName = TermName(origin.name.toString)
            val targetName = TermName(target.name.toString)
            val freshTerms = List.fill(getCaseClassParams(target).length)(TermName(c.freshName))
            val binds      = freshTerms.map(x => Bind(x, Ident(termNames.WILDCARD)))
            val args       = freshTerms.map(x => Ident(x))
            cq"$originName(..$binds) => $targetName[..$claitTypeParamNamesAsTrees](..$args)"
          case (origin, target: ModuleDef) =>
            val originName = TermName(origin.name.toString)
            val targetName = TermName(target.name.toString)
            cq"$originName => $targetName"
        }

      val mtch = Match(EmptyTree, embedAlgebraCases)

      val algebra =
        q"""
        new qq.droste.GAlgebra[λ, ${clait.name}[..$claitTypeParamNames], ${clait.name}[..$claitTypeParamNames]]($mtch)
        """

      q"def embedAlgebra[..${clait.tparams}]: qq.droste.Algebra[λ, ${clait.name}[..$claitTypeParamNames]] = $algebra"

    }

    val toFixedPoint: DefDef = {
      val embedAlgebraCases: List[CaseDef] =
        (AdtCases zip NonRecursiveAdtCases) map {
          case (origin: ClassDef, target) =>
            val originName = TermName(origin.name.toString)
            val targetName = TermName(target.name.toString)
            val freshTerms = List.fill(getCaseClassParams(target).length)(TermName(c.freshName))
            val binds      = freshTerms.map(x => Bind(x, Ident(termNames.WILDCARD)))
            val args       = freshTerms.map(x => Ident(x))

            cq"$originName(..$binds) => $targetName[..$claitTypeParamNamesAsTrees, ${clait.name}[..$claitTypeParamNames]](..$args)"
          case (origin: ModuleDef, target) =>
            val targetName = TermName(target.name.toString)
            val originName = TermName(origin.name.toString)
            cq"$originName => $targetName[..$claitTypeParamNamesAsTrees, ${clait.name}[..$claitTypeParamNames]]()"
        }

      val mtch = c.untypecheck(Match(EmptyTree, embedAlgebraCases))

      val algebra =
        q"""
        new qq.droste.GCoalgebra[λ, ${clait.name}[..$claitTypeParamNames], ${clait.name}[..$claitTypeParamNames]]($mtch)
        """

      q"def projectCoalgebra[..${clait.tparams}]: qq.droste.Coalgebra[λ, ${clait.name}[..$claitTypeParamNames]] = $algebra"
    }

    val basisInstance: DefDef = {
      q"""
      implicit def basisInstance[..${clait.tparams}]: qq.droste.Basis[λ, ${clait.name}[..$claitTypeParamNames]] =
        qq.droste.Basis.Default(embedAlgebra, projectCoalgebra)
      """
    }

    val deriveFixedPointModule: ModuleDef = q"""
      object fixedpoint {
        $nonRecursiveAdt
        ..$NonRecursiveAdtCases
        $λ
        $traverseInstance
        $toFixedPoint
        $toRecursive
        $basisInstance
      }
      """

    val outputs =
      clait match {
        case classOrTrait: ClassDef if isSealed(classOrTrait) =>
          List(
            clait,
            ModuleDef(
              companion.mods,
              companion.name,
              Template(
                companion.impl.parents,
                companion.impl.self,
                companion.impl.body :+ deriveFixedPointModule
              )
            )
          )

        case _ =>
          sys.error("@deriveFixedPoint should only annotate sealed traits or sealed abstract classes")
      }

    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }
}
