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

trait OptionalFieldOperations {
  import ExtraImplicits._

  /**
   * The `setOrUpdateField` operation sets or updates an optionalField.
   */
  def setOrUpdateField[T: Reader: JsonWriter](default: ⇒ T)(f: T ⇒ T): Operation =
    updateOptionalField[T](_.map(f).orElse(Some(default)))

  /**
   * The `modifyOrDeleteField` operation works together with the `optionalField` lens.
   * The passed function is called for every existing field. If the function returns
   * `Some(value)`, this will become the new value. If the function returns `None` the
   * field will be deleted.
   */
  def modifyOrDeleteField[T: Reader: JsonWriter](f: T ⇒ Option[T]): Operation =
    updateOptionalField[T](_.flatMap(f))

  /**
   * The `updateOptionalField` operation works together with the `optionalField` lens. It allows
   * to a) create a previously missing field b) update an existing field value c) remove an existing
   * field d) ignore a missing field.
   */
  def updateOptionalField[T: Reader: JsonWriter](f: Option[T] ⇒ Option[T]): Operation = new Operation {
    def apply(value: SafeJsValue): SafeJsValue = {
      val oldValue = value.flatMap(_.as[T]) match {
        case Right(v)                                 ⇒ Right(Some(v))
        case OptionLenses.FieldMissing                ⇒ Right(None)
        case l: Left[Exception, Option[T]] @unchecked ⇒ l
      }
      oldValue flatMap (v ⇒ f(v).map(x ⇒ Right(x.toJson)).getOrElse(OptionLenses.FieldMissing))
    }
  }
}
