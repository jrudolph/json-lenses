package spray.json
package lenses

/**
 * An aggregate option to import all of the functionality of JsonLenses with one
 * import.
 */
object JsonLenses extends
  ScalarLenses with
  OptionLenses with
  SeqLenses with
  Operations with
  JsonPathIntegration with
  ExtraImplicits {

  class OptionalFieldBuilder(fieldName: String) {
    def `?`: OptLens = optionalField(fieldName)
  }

  implicit def strToField(name: String): ScalarLens = field(name)
  implicit def symbolToField(sym: Symbol): ScalarLens = field(sym.name)
  implicit def strToPossiblyOptionalField(name: String): OptionalFieldBuilder = new OptionalFieldBuilder(name)
  implicit def strToPossiblyOptionalField(sym: Symbol): OptionalFieldBuilder = new OptionalFieldBuilder(sym.name)

  /**
   * The lens which combines an outer lens with an inner.
   */
  def combine[M[_], M2[_], R[_], T1, T2, T3](outer: GeneralLens[M, T1, T2], inner: GeneralLens[M2, T2, T3])(implicit ev: Join[M2, M, R]): GeneralLens[R, T1, T3] =
    new GeneralLensImpl[R, T1, T3]()(ev.get(inner.ops, outer.ops)) {
      def retr: T1 => Validated[R[T3]] = parent =>
        for {
          outerV <- outer.retr(parent)
          innerV <- ops.allRight(outer.ops.flatMap(outerV)(x => inner.ops.toSeq(inner.retr(x))))
        } yield innerV

      def updated(f: GeneralOperation)(parent: T1): Validated[T1] =
        outer.updated(_.flatMap(inner.updated(f)))(parent)
    }
}
