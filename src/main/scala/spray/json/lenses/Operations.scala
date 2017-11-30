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
 * Defines a set of operations to update Json values.
 */
trait Operations extends OptionalFieldOperations { _: ExtraImplicits ⇒
  /**
   * The set operation sets or creates a value.
   */
  def set[T: JsonWriter](t: ⇒ T): Operation = new Operation {
    def apply(value: SafeJsValue): SafeJsValue =
      // ignore existence of old value
      Right(t.toJson)
  }

  /**
   * A MapOperation is one that expect an old value to be available.
   */
  trait MapOperation extends Operation {
    def apply(value: JsValue): SafeJsValue

    def apply(value: SafeJsValue): SafeJsValue = value.flatMap(apply)
  }

  /**
   * The `modify` operation applies a function on the (converted) value
   */
  def modify[T: Reader: JsonWriter](f: T ⇒ T): Operation = new MapOperation {
    def apply(value: JsValue): SafeJsValue =
      value.as[T] map (v ⇒ f(v).toJson)
  }

  def append(update: Update): Operation = ???
  def update(update: Update): Operation = new MapOperation {
    def apply(value: JsValue): SafeJsValue =
      safe(update(value))
  }
  def extract[M[_], T](value: Lens[M])(f: M[T] ⇒ Update): Operation = ???
}

object Operations extends Operations with ExtraImplicits
