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

trait ExtraImplicits {
  trait RichJsValue {
    def value: JsValue

    def update(updater: Update): JsValue = updater(value)

    def update[T: JsonWriter, M[_]](lens: UpdateLens, pValue: T): JsValue =
      lens ! Operations.set(pValue) apply value

    // This can't be simplified because we don't want the type constructor
    // for Lens[M] to appear in the type parameter list.
    def extract[T: Reader](p: Lens[Id]): T =
      p.get[T](value)

    def extract[T: Reader](p: Lens[Option]): Option[T] =
      p.get[T](value)

    def extract[T: Reader](p: Lens[Seq]): Seq[T] =
      p.get[T](value)

    def as[T: Reader]: Validated[T] =
      implicitly[Reader[T]].read(value)
  }

  implicit def richValue(v: JsValue): RichJsValue = new RichJsValue { def value = v }
  implicit def richString(str: String): RichJsValue = new RichJsValue { def value = JsonParser(str) }
}

object ExtraImplicits extends ExtraImplicits
