package spray.json
package lenses

/**
 * Defines a set of operations to update Json values.
 */
trait Operations extends OptionalFieldOperations { _: ExtraImplicits =>
  /**
   * The set operation sets or creates a value.
   */
  def set[T: JsonWriter](t: => T): Operation = new Operation {
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
  def modify[T: Reader : JsonWriter](f: T => T): Operation = new MapOperation {
    def apply(value: JsValue): SafeJsValue =
      value.as[T] map (v => f(v).toJson)
  }

  def append(update: GeneralUpdate[JsValue]): Operation = ???
  def update(update: GeneralUpdate[JsValue]): Operation = ???
  def extract[M[_], T](value: Lens[M])(f: M[T] => GeneralUpdate[JsValue]): Operation = ???
}

object Operations extends Operations with ExtraImplicits
