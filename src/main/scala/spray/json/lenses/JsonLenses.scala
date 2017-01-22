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
 * An aggregate option to import all of the functionality of JsonLenses with one
 * import.
 */
object JsonLenses extends ScalarLenses with OptionLenses with SeqLenses with Operations with JsonPathIntegration with ExtraImplicits {

  class OptionalFieldBuilder(fieldName: String) {
    def `?`: OptLens = optionalField(fieldName)
  }

  implicit def strToField(name: String): ScalarLens = field(name)
  implicit def symbolToField(sym: Symbol): ScalarLens = field(sym.name)
  implicit def strToPossiblyOptionalField(name: String): OptionalFieldBuilder = new OptionalFieldBuilder(name)
  implicit def strToPossiblyOptionalField(sym: Symbol): OptionalFieldBuilder = new OptionalFieldBuilder(sym.name)

  /**
   * The lens which combines an outer lens with an inner.
   */
  def combine[M[_], M2[_], R[_]](outer: Lens[M], inner: Lens[M2])(implicit ev: Join[M2, M, R]): Lens[R] =
    new LensImpl[R]()(ev.get(inner.ops, outer.ops)) {
      def retr: JsValue ⇒ Validated[R[JsValue]] = parent ⇒
        for {
          outerV ← outer.retr(parent)
          innerV ← ops.allRight(outer.ops.flatMap(outerV)(x ⇒ inner.ops.toSeq(inner.retr(x))))
        } yield innerV

      def updated(f: SafeJsValue ⇒ SafeJsValue)(parent: JsValue): SafeJsValue =
        outer.updated(_.flatMap(inner.updated(f)))(parent)
    }
}
