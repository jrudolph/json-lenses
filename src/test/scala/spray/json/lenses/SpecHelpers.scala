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

import org.specs2.mutable.Specification
import scala.reflect.ClassTag
import spray.json.JsonParser

trait SpecHelpers {
  self: Specification =>

  import JsonLenses._

  def be_json(json: String) =
    be_==(JsonParser(json))

  import org.specs2.matcher.{ BeMatching, Matcher }

  override def throwA[E <: Throwable](message: String = ".*")(implicit m: ClassTag[E]): Matcher[Any] = {
    import java.util.regex.Pattern
    throwA(m).like {
      case e => createExpectable(e.getMessage).applyMatcher(new BeMatching(".*" + Pattern.quote(message) + ".*"))
    }
  }
}
