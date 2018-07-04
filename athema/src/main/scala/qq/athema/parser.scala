package qq.athema

import scala.collection.immutable.Queue

import atto._
import atto.syntax.all._

import cats._
import cats.data.State
import cats.syntax.all._

import qq.droste._

object ExprParser extends App {

  type Token = Expr[BigDecimal, Null]

  val TAdd : Token = Add(null, null)
  val TSub : Token = Sub(null, null)
  val TProd: Token = Prod(null, null)
  val TDiv : Token = Div(null, null)
  val TNeg : Token = Neg(null)

  type TConst = Const[BigDecimal, Null]
  type TVar   = Var[BigDecimal, Null]

  val token: Parser[Token] =
    Atto.token(
      Atto.char('+').as(TAdd)           |
      Atto.char('-').as(TSub)           |
      Atto.char('*').as(TProd)          |
      Atto.char('/').as(TDiv)           |
      Atto.bigDecimal.map(Const(_): Token) |
      identifier.map(Var(_))
    )

  val identifier: Parser[String] =
    (Atto.letter, Atto.many(Atto.letterOrDigit))
      .mapN((h, t) => (h :: t).mkString)

  val tokens: Parser[List[Token]] =
    Atto.many(token)

  final case class S(
    input: List[Token],
    ops: List[(Int, Token)],
    output: Queue[Token])

  val step: S => Either[S, List[Token]] = state => {

    def operator(s: S, input: List[Token], op: Token, p: Int): Either[S, List[Token]] = {
      val (oops, ops) = s.ops.span(_._1 > p)
      S(input, (p, op) :: ops, s.output.enqueue(oops.map(_._2))).asLeft
    }

    def operand(s: S, input: List[Token], op: Token): Either[S, List[Token]] =
      S(input, s.ops, s.output.enqueue(op)).asLeft

    state.input match {
      case head :: tail =>
        head match {
          case _: TConst => operand (state, tail, head)
          case _: TVar   => operand (state, tail, head)
          case TAdd      => operator(state, tail, head, 1)
          case TSub      => operator(state, tail, head, 2)
          case TProd     => operator(state, tail, head, 2)
          case TDiv      => operator(state, tail, head, 2)
          case TNeg      => operator(state, tail, head, 3)
        }
      case Nil =>
        state.output.enqueue(state.ops.map(_._2)).toList.asRight
    }
  }

  val shuntingYard = tokens.map(input => Monad[Id].tailRecM(S(input, List.empty, Queue.empty))(step))

  println(shuntingYard.parseOnly("1 - 2 * 3 + 4"))

  println(shuntingYard.parseOnly("1 / 2 * 3 + 4 - 0 - 10 * 20 + 1"))

}
