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

trait ScalarLenses {
  /**
   * Accesses a field of a JsObject.
   */
  def field(name: String): ScalarLens = new LensImpl[Id] {
    def updated(f: SafeJsValue => SafeJsValue)(parent: JsValue): SafeJsValue =
      for (updatedValue <- f(retr(parent)))
        // asJsObject is already guarded by getField above, FIXME: is it really?
        yield JsObject(fields = parent.asJsObject.fields + (name -> updatedValue))

    def retr: JsValue => SafeJsValue = v => asObj(v) flatMap {
      _.fields.get(name).getOrError("Expected field '%s' in '%s'" format (name, v))
    }

    def asObj(v: JsValue): Validated[JsObject] = v match {
      case o: JsObject => Right(o)
      case e @ _       => unexpected("Not a json object: " + e)
    }
  }

  /**
   * Accesses an element of a JsArray.
   */
  def element(idx: Int): ScalarLens = new LensImpl[Id] {
    def updated(f: SafeJsValue => SafeJsValue)(parent: JsValue): SafeJsValue = parent match {
      case JsArray(elements) =>
        if (idx < elements.size) {
          val (headEls, element +: tail) = elements.splitAt(idx)
          f(Right(element)) map (v => JsArray(headEls ++ (v +: tail)))
        } else
          unexpected("Too little elements in array: %s size: %d index: %d" format (parent, elements.size, idx))
      case e @ _ =>
        unexpected("Not a json array: " + e)
    }

    def retr: JsValue => SafeJsValue = {
      case a @ JsArray(elements) =>
        if (idx < elements.size)
          Right(elements(idx))
        else
          outOfBounds("Too little elements in array: %s size: %d index: %d" format (a, elements.size, idx))
      case e @ _ => unexpected("Not a json array: " + e)
    }
  }

  /**
   * The identity lens which operates on the current element itself
   */
  val value: ScalarLens = new LensImpl[Id] {
    def updated(f: SafeJsValue => SafeJsValue)(parent: JsValue): SafeJsValue =
      f(Right(parent))

    def retr: JsValue => SafeJsValue = x => Right(x)
  }

  /**
   * A lens which leaves JsArray as is but transforms any other kind of JsValue into
   * a singleton JsArray with that value as single element.
   */
  val arrayOrSingletonAsArray: ScalarLens = new LensImpl[Id] {
    def updated(f: SafeJsValue => SafeJsValue)(parent: JsValue): SafeJsValue =
      retr(parent).flatMap(x => f(Right(x)))

    def retr: JsValue => Validated[JsValue] = {
      case ar: JsArray => Right(ar)
      case x           => Right(JsArray(x))
    }
  }
}

object ScalarLenses extends ScalarLenses
