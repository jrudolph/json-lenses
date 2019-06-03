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

trait JsonPathIntegration { self: ScalarLenses with SeqLenses with OptionLenses =>
  /**
   * Create a Lens from a json-path expression.
   */
  def fromPath(path: String): Lens[Seq] =
    fromPath(JsonPathParser(path))

  def fromPath(ast: JsonPath.Path): Lens[Seq] = {
    def convertPath(path: JsonPath.Path): Lens[Seq] = path match {
      case JsonPath.Root                   => value.toSeq
      case JsonPath.Selection(inner, proj) => convertPath(inner) / convertLens(proj)
    }
    def convertLens(proj: JsonPath.Projection): Lens[Seq] =
      proj match {
        case JsonPath.ByField(name)     => optionalField(name).toSeq
        case JsonPath.ByIndex(i)        => element(i).toSeq
        case JsonPath.AllElements       => elements
        case JsonPath.ByPredicate(pred) => filter(convertPredicate(pred))
      }
    def convertPredicate(pred: JsonPath.Predicate): JsPred = pred match {
      case op: JsonPath.BinOpPredicate =>
        val f1 = convertExpr(op.expr1)
        val f2 = convertSimpleExpr(op.expr2)

        js => {
          val v2 = f2(js)
          f1(js).right.forall(_.forall(v1 => op.predicate(v1, v2)))
        }

        case JsonPath.Exists(path) =>
        js => convertPath(path).retr(js).exists(_.nonEmpty)
    }
    def convertExpr(expr: JsonPath.Expr): JsValue => Validated[Seq[JsValue]] = expr match {
      case JsonPath.PathExpr(path) => js => convertPath(path).retr(js)
      case simple: JsonPath.SimpleExpr => js => Right(Seq(convertSimpleExpr(simple)(js)))
    }
    def convertSimpleExpr(expr: JsonPath.SimpleExpr): JsValue => JsValue = expr match {
      case JsonPath.Constant(x) => _ => x
    }

    convertPath(ast)
  }
}
