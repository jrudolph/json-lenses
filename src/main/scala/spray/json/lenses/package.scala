package spray.json

package object lenses {
  type JsPred = JsValue => Boolean
  type Id[T] = T
  type Validated[T] = Either[Exception, T]
  type SafeJsValue = Validated[JsValue]

  type Operation = SafeJsValue => SafeJsValue

  type ScalarLens = Lens[Id]
  type OptLens = Lens[Option]
  type SeqLens = Lens[Seq]

  type UpdateLens = GeneralUpdateLens[JsValue, JsValue]

  def ??? = sys.error("NYI")
  def unexpected(message: String) = Left(new RuntimeException(message))
  def outOfBounds(message: String) = Left(new IndexOutOfBoundsException(message))

  implicit def rightBiasEither[A, B](e: Either[A, B]): Either.RightProjection[A, B] = e.right

  case class GetOrThrow[B](e: Either[Throwable, B]) {
    def getOrThrow: B = e match {
      case Right(b) => b
      case Left(e) => throw new RuntimeException(e)
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

  def safe[T](body: => T): Validated[T] =
    try {
      Right(body)
    } catch {
      case e: Exception => Left(e)
    }

  case class ValidateOption[T](option: Option[T]) {
    def getOrError(message: => String): Validated[T] = option match {
      case Some(t) => Right(t)
      case None => unexpected(message)
    }
  }

  implicit def validateOption[T](o: Option[T]): ValidateOption[T] = ValidateOption(o)

  implicit def addExtraReadLensMethodsToString(fieldName: String): ExtraReadLensMethods[Id] = addExtraReadLensMethods(JsonLenses.strToField(fieldName))
  implicit def addExtraReadLensMethods[M[_]](lens: Lens[M]): ExtraReadLensMethods[M] = new ExtraReadLensMethods[M] {
    import ExtraImplicits.richValue
    import lens.{ ops, retr }

    def tryGet[T: Reader](p: JsValue): Validated[M[T]] =
      retr(p).flatMap(mapValue(_)(_.as[T]))

    def get[T: Reader](p: JsValue): M[T] =
      tryGet[T](p).getOrThrow

    def is[U: Reader](f: U => Boolean): JsPred = value =>
      tryGet[U](value) exists (x => ops.map(x)(f).forall(identity))

    private[this] def mapValue[T](value: M[JsValue])(f: JsValue => Validated[T]): Validated[M[T]] =
      ops.allRight(ops.map(value)(f))
  }
}
