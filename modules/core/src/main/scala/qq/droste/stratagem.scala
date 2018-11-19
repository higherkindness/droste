package qq.droste

import cats.Applicative
import cats.Functor
import cats.Traverse
import cats.instances.list._
import cats.syntax.functor._

import data.prelude._
import data.Attr
import data.AttrF
import data.Coattr
import data.Fix

/** Miscellaneous recursion scheme variations, tools, and helpers.
  *
  * These aren't organized in any particular manner. Methods
  * in here are more likely to be renamed/reorganized/adjusted
  * than methods elsewhere in Droste.
  */
object stratagem {

  def attributeCata[F[_]: Functor, A](algebra: Algebra[F, A]): Fix[F] => Attr[F, A] =
    scheme.cata(Trans((fa: F[Attr[F, A]]) => AttrF(algebra(fa.map(_.head)), fa)).algebra)

  /** An algebra for listing all possible partial structures */
  def allPartials[F[_]: Traverse, A]: Algebra[AttrF[F, A, ?], List[Coattr[F, A]]] =
    partials(Applicative[List])

  /** An algebra for listing partial structures showing the path a fold might
    * take through a data structure
    */
  def perimeterPartials[F[_]: Traverse, A]: Algebra[AttrF[F, A, ?], List[Coattr[F, A]]] =
    partials(listPerimeterApplicative)

  /** An algebra for converting an annotated structure into a list of partial
    * structures using the annotations to halt the structure.
    *
    * This is useful for snapshotting the intermediate data structures during a
    * fold.
    */
  def partials[F[_]: Traverse, A](app: Applicative[List]): Algebra[AttrF[F, A, ?], List[Coattr[F, A]]] =
    Algebra(envt => Coattr.pure[F, A](envt.ask) :: Traverse[F].sequence(envt.lower)(app).map(Coattr.roll))

  private lazy val listPerimeterApplicative: Applicative[List] = new Applicative[List] {
    def pure[A](x: A): List[A] = List(x)

    /*
     * Instead of taking the whole cross product, we just pick one path along
     * the perimeter through the solution space.
     *
     * A normal applicative instance would evaluate the cross product:
     *
     *           ff
     *    .----------------
     *    |      a   b   c
     *    |   .------------
     *    | 1 | 1a  1b  1c
     * fa | 2 | 2a  2b  2c
     *    | 3 | 3a  3b  3c
     *
     * This applicative instance picks results along one perimeter:
     *
     *           ff
     *    .----------------
     *    |      a   b   c
     *    |   .------------
     *    | 1 | 1a
     * fa | 2 | 2a
     *    | 3 | 3a  3b  3c
     *
     */
    def ap[A, B](ff: List[A => B])(fa: List[A]): List[B] =
      ff match {
        case Nil         => Nil
        case head :: Nil => fa.map(head)
        case head :: tail =>
          fa match {
            case Nil      => Nil
            case a :: Nil => head(a) :: Nil
            case _ =>
              val last = fa.last
              fa.map(head) ::: tail.map(f => f(last))
          }
      }

  }
}
