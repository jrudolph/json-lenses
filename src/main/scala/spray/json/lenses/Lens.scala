package spray.json
package lenses

/**
 * A Lens combines read and update functions of UpdateLens and ReadLens into
 * combinable chunks.
 *
 * A lens can either operate on a scalar value, or on an optional value, or on a
 * sequence value. This is denoted by the `M[_]` type constructor.
 */
trait GeneralLens[M[_], P, T] extends GeneralUpdateLens[P, T] with GeneralReadLens[M, P, T] {
  /**
   * A shortcut for the `combine` lens which combines two lenses.
   */
  def /[M2[_], R[_], T2](next: GeneralLens[M2, T, T2])(implicit ev: Join[M2, M, R]): GeneralLens[R, P, T2]

  def toSeq: GeneralLens[Seq, P, T]

  def ops: Ops[M]
}

/**
 * This implements most of the methods of `Lens`. Implementors of a new type of lens
 * must implement `retr` for the read side of the lens and `updated` for the update side of the lens.
 */
abstract class GeneralLensImpl[M[_], P, T](implicit val ops: Ops[M]) extends GeneralLens[M, P, T] { outer =>
  def /[M2[_], R[_], T2](next: GeneralLens[M2, T, T2])(implicit ev: Join[M2, M, R]): GeneralLens[R, P, T2] =
    JsonLenses.combine(this, next)

  def toSeq: GeneralLens[Seq, P, T] = this / SeqLenses.asSeq
}
