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

import scala.language.implicitConversions

package object lenses {
  type JsPred = JsValue ⇒ Boolean
  type Id[T] = T
  type Validated[T] = Either[Exception, T]
  type SafeJsValue = Validated[JsValue]

  type Operation = SafeJsValue ⇒ SafeJsValue

  type ScalarLens = Lens[Id]
  type OptLens = Lens[Option]
  type SeqLens = Lens[Seq]

  def ??? = sys.error("NYI")
  def unexpected(message: String) = Left(new RuntimeException(message))
  def outOfBounds(message: String) = Left(new IndexOutOfBoundsException(message))

  implicit def rightBiasEither[A, B](e: Either[A, B]): Either.RightProjection[A, B] = e.right

  case class GetOrThrow[B](e: Either[Throwable, B]) {
    def getOrThrow: B = e match {
      case Right(b) ⇒ b
      case Left(e)  ⇒ throw new RuntimeException(e)
    }
  }

  implicit def orThrow[B](e: Either[Throwable, B]): GetOrThrow[B] = GetOrThrow(e)

  trait Reader[T] {
    def read(js: JsValue): Validated[T]
  }

  object Reader {
    implicit def safeMonadicReader[T: JsonReader]: Reader[T] = new Reader[T] {
      def read(js: JsValue): Validated[T] =
        safe(js.convertTo[T])
    }
  }

  def safe[T](body: ⇒ T): Validated[T] =
    try Right(body)
    catch {
      case e: Exception ⇒ Left(e)
    }

  case class ValidateOption[T](option: Option[T]) {
    def getOrError(message: ⇒ String): Validated[T] = option match {
      case Some(t) ⇒ Right(t)
      case None    ⇒ unexpected(message)
    }
  }

  implicit def validateOption[T](o: Option[T]): ValidateOption[T] = ValidateOption(o)
}
