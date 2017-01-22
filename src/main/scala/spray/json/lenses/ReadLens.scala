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
 * The read lens can extract child values out of a JsValue hierarchy. A read lens
 * is parameterized with a type constructor. This allows to extracts not only scalar
 * values but also sequences or optional values.
 * @tparam M
 */
trait ReadLens[M[_]] {
  /**
   * Given a parent JsValue, tries to extract the child value.
   * @return `Right(value)` if the lens read succeeds. `Left(error)` if the lens read fails.
   */
  def retr: JsValue ⇒ Validated[M[JsValue]]

  /**
   * Given a parent JsValue extracts and tries to convert the JsValue into
   * a value of type `T`
   */
  def tryGet[T: Reader](value: JsValue): Validated[M[T]]

  /**
   * Given a parent JsValue extracts and converts a JsValue into a value of
   * type `T` or throws an exception.
   */
  def get[T: Reader](value: JsValue): M[T]

  /**
   * Lifts a predicate for a converted value for this lens up to the
   * parent level. The returned predicate will return false for values
   * which fail to read.
   */
  def is[U: Reader](f: U ⇒ Boolean): JsPred
}