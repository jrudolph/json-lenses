package spray.json
package lenses

trait GeneralUpdate[T] extends (T => T) { outer =>
  def apply(value: T): T

  def &&(next: GeneralUpdate[T]): GeneralUpdate[T] = new GeneralUpdate[T] {
    def apply(value: T): T = next(outer(value))
  }
}

trait Update extends GeneralUpdate[JsValue] {
  def apply(jsonString: String): String =
    apply(JsonParser(jsonString)).toString()
}


/**
 * The UpdateLens is the central interface for updating a child element somewhere
 * deep down a hierarchy of a JsValue.
 */
trait GeneralUpdateLens[P, T] {
  type GeneralOperation = Validated[T] => Validated[T]
  /**
   * Applies function `f` on the child of the `parent` denoted by this UpdateLens
   * and returns a `Right` of the parent with the child element updated.
   *
   * The value passed to `f` may be `Left(e)` if the child could not be found
   * in which case particular operations may still succeed. Function `f` may return
   * `Left(error)` in case the operation fails.
   *
   * `updated` returns `Left(error)` if the update operation or any of any intermediate
   * lens fails.
   */
  def updated(f: GeneralOperation)(parent: P): Validated[JsValue]

  def !(op: GeneralOperation): GeneralUpdate[T]
}

