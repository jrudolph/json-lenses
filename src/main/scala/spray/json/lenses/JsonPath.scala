/*
 *    Copyright 2012-2017 Johannes Rudolph
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package spray.json
package lenses

/**
 * The AST for a json-path. Basically follows the specification at
 * [[http://goessner.net/articles/JsonPath/]]
 */
object JsonPath {
  sealed trait Path
  case object Root extends Path
  case class Selection(previous: Path, projection: Projection) extends Path

  sealed trait Projection
  case object AllElements extends Projection
  case class ByField(name: String) extends Projection
  case class ByIndex(idx: Int) extends Projection
  case class ByPredicate(expr: Predicate) extends Projection

  sealed trait Predicate
  sealed trait BinOpPredicate extends Predicate {
    def expr1: Expr
    def expr2: SimpleExpr

    def predicate(v1: JsValue, v2: JsValue): Boolean
  }
  case class Eq(expr1: Expr, expr2: SimpleExpr) extends BinOpPredicate {
    def predicate(v1: JsValue, v2: JsValue): Boolean = v1 == v2
  }
  case class Lt(expr1: Expr, expr2: SimpleExpr) extends BinOpPredicate {
    def predicate(v1: JsValue, v2: JsValue): Boolean = (v1, v2) match {
      case (JsNumber(n1), JsNumber(n2)) => n1 < n2
      case _                            => false
    }
  }
  case class Gt(expr1: Expr, expr2: SimpleExpr) extends BinOpPredicate {
    def predicate(v1: JsValue, v2: JsValue): Boolean = (v1, v2) match {
      case (JsNumber(n1), JsNumber(n2)) => n1 > n2
      case _                            => false
    }
  }
  case class Exists(path: Path) extends Predicate

  sealed trait Expr
  sealed trait SimpleExpr extends Expr
  case class PathExpr(path: Path) extends Expr
  case class Constant(value: JsValue) extends SimpleExpr
}