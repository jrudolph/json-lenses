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

package spray.json.lenses

import scala.annotation.tailrec
import scala.collection.immutable.VectorBuilder
import scala.language.higherKinds

/**
 * A trait to define common operations for different container types.
 * There's some bias towards `Seq` because container types have to support
 * conversions towards and from `Seq`.
 *
 * This could probably made more general but the methods defined here comprise
 * exactly the set of operations needed to allow combining different kinds of
 * lenses.
 */
trait Ops[M[_]] {
  def flatMap[T, U](els: M[T])(f: T => Seq[U]): Seq[U]

  /**
   * Checks if all the elements of the Seq are valid and then
   * packages them into a validated container.
   */
  def allRight[T](v: Seq[Validated[T]]): Validated[M[T]]

  /**
   * Converts a validated container of `T`s into a sequence
   * of validated values.
   */
  def toSeq[T](v: Validated[M[T]]): Seq[Validated[T]]

  def map[T, U](els: M[T])(f: T => U): Seq[U] =
    flatMap(els)(v => Seq(f(v)))
}

object Ops {
  implicit def idOps: Ops[Id] = new Ops[Id] {
    def flatMap[T, U](els: T)(f: T => Seq[U]): Seq[U] = f(els)

    def allRight[T](v: Seq[Validated[T]]): Validated[T] = v.head

    def toSeq[T](v: Validated[T]): Seq[Validated[T]] = Seq(v)
  }

  implicit def optionOps: Ops[Option] = new Ops[Option] {
    def flatMap[T, U](els: Option[T])(f: T => Seq[U]): Seq[U] =
      els.toSeq.flatMap(f)

    def allRight[T](v: Seq[Validated[T]]): Validated[Option[T]] =
      v match {
        case Nil           => Right(None)
        case Seq(Right(x)) => Right(Some(x))
        case Seq(Left(e))  => Left(e)
      }

    def toSeq[T](v: Validated[Option[T]]): Seq[Validated[T]] = v match {
      case Right(Some(x)) => Seq(Right(x))
      case Right(None)    => Nil
      case Left(e)        => Seq(Left(e))
    }
  }

  implicit def seqOps: Ops[Seq] = new Ops[Seq] {
    def flatMap[T, U](els: Seq[T])(f: T => Seq[U]): Seq[U] =
      els.flatMap(f)

    def allRight[T](v: Seq[Validated[T]]): Validated[Seq[T]] = {
      @tailrec def inner(l: Seq[Validated[T]], acc: VectorBuilder[T]): Validated[Seq[T]] =
        l match {
          case Right(head) +: tail =>
            acc += head
            inner(tail, acc)
          case Left(e) +: _ => Left(e)
          case Nil          => Right(acc.result())
        }
      inner(v, new VectorBuilder)
    }

    def toSeq[T](x: Validated[Seq[T]]): Seq[Validated[T]] = x match {
      case Right(x) => x.map(Right(_))
      case Left(e)  => List(Left(e))
    }
  }
}