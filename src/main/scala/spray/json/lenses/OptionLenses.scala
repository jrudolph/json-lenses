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

trait OptionLenses {
  /**
   * Operates on the first element of an JsArray which matches the predicate.
   */
  def find(pred: JsPred): OptLens = new LensImpl[Option] {
    def updated(f: SafeJsValue ⇒ SafeJsValue)(parent: JsValue): SafeJsValue = parent match {
      case JsArray(elements) ⇒
        elements.span(x ⇒ !pred(x)) match {
          case (prefix, element +: suffix) ⇒
            f(Right(element)) map (v ⇒ JsArray(prefix ++ (v +: suffix)))

          // element not found, do nothing
          case _ ⇒
            Right(parent)
        }
      case e @ _ ⇒ unexpected("Not a json array: " + e)
    }

    def retr: JsValue ⇒ Validated[Option[JsValue]] = {
      case JsArray(elements) ⇒ Right(elements.find(pred))
      case e @ _             ⇒ unexpected("Not a json array: " + e)
    }
  }

  /**
   * Accesses a maybe missing field of a JsObject.
   */
  def optionalField(name: String): OptLens = new LensImpl[Option] {
    import OptionLenses._
    def updated(f: SafeJsValue ⇒ SafeJsValue)(parent: JsValue): SafeJsValue =
      retr(parent).flatMap { oldValueO ⇒
        f(oldValueO.map(Right(_)).getOrElse(FieldMissing)) match {
          case FieldMissing ⇒ Right(JsObject(fields = parent.asJsObject.fields - name))
          case x            ⇒ x.map(newVal ⇒ JsObject(fields = parent.asJsObject.fields + (name -> newVal)))
        }
      }

    def retr: JsValue ⇒ Validated[Option[JsValue]] = {
      case o: JsObject ⇒ Right(o.fields.get(name))
      case e @ _       ⇒ unexpected("Not a json object: " + e)
    }
  }
}

object OptionLenses extends OptionLenses {
  val FieldMissing = unexpected("Field missing")
}
