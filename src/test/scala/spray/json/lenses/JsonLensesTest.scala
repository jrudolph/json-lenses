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

import Predef.{ augmentString => _, wrapString => _, _ }
import DefaultJsonProtocol._
import spray.json.{ JsValue, JsonParser }

object JsonLensesTest extends App {

  import JsonLenses._

  "n" ! set(3)
  "els" ! append {
    "name" ! set("Peter") &&
      "money" ! set(2)
  }
  "els" / element(1) ! update {
    "money" ! set(38) &&
      "name" ! set("Testperson")
  } && "n" ! modify[Int](_ + 1)

  val json = JsonParser("test")
  val newJson = json("els" / "money") = 12

  val i = json.extract[Int]("els" / "money")

  ("els" / element(1) / "money").get[Int] _: (JsValue => Int)

  ("els" / find("money".is[Int](_ < 30)) / "name").get[String]: (JsValue => Option[String])

  ("els" / * / "money").get[Int] _: (JsValue => Seq[Int])
  ("els" / filter("money".is[Int](_ < 30)) / "name").get[String] _: (JsValue => Seq[String])
  "els" / filter("money".is[Int](_ < 30)) / "name" ! modify[String]("Richman " + _)

  //: JsValue => JsValue

  def updateMoney(x: Int) =
    "money" ! modify[Int](_ + x)

  "els" / * ! update(updateMoney(12))
  "els" / * ! extract("name") {
    name: String =>
      updateMoney(name.length)
  }
}
