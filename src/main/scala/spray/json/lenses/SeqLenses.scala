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

import scala.language.higherKinds

trait SeqLenses {
  /**
   * The lens which just converts another Lens into one of a
   * Seq value.
   */
  val asSeq: SeqLens = new LensImpl[Seq] {
    def updated(f: Operation)(parent: JsValue): SafeJsValue =
      f(Right(parent))

    def retr: JsValue ⇒ Validated[Seq[JsValue]] = x ⇒ Right(Seq(x))
  }

  /**
   * All the elements of a JsArray.
   */
  val elements: SeqLens = new LensImpl[Seq] {
    def updated(f: SafeJsValue ⇒ SafeJsValue)(parent: JsValue): SafeJsValue = parent match {
      case JsArray(elements) ⇒
        ops.allRight(elements.map(x ⇒ f(Right(x)))).map(JsArray(_: _*))
      case e @ _ ⇒ unexpected("Not a json array: " + e)
    }

    def retr: JsValue ⇒ Validated[Seq[JsValue]] = {
      case JsArray(elements) ⇒ Right(elements)
      case e @ _             ⇒ unexpected("Not a json array: " + e)
    }
  }

  /**
   * Like `elements` but filters elements where the `inner` lens doesn't apply
   */
  def allMatching[M[_]](inner: Lens[M]): SeqLens =
    filter(inner.retr(_).isRight) / inner.toSeq

  /**Alias for `elements`*/
  def * = elements

  /**
   * All the values of a JsArray which match the predicate.
   */
  def filter(pred: JsPred): SeqLens = new LensImpl[Seq] {
    def updated(f: SafeJsValue ⇒ SafeJsValue)(parent: JsValue): SafeJsValue = parent match {
      case JsArray(elements) ⇒
        ops.allRight(elements.map(x ⇒ if (pred(x)) f(Right(x)) else Right(x))).map(JsArray(_: _*))

      case e @ _ ⇒ unexpected("Not a json array: " + e)
    }

    def retr: JsValue ⇒ Validated[Seq[JsValue]] = {
      case JsArray(elements) ⇒
        Right(elements.filter(pred))
      case e @ _ ⇒ unexpected("Not a json array: " + e)
    }
  }
}

object SeqLenses extends SeqLenses
