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

/*
 * Tests imported from http://code.google.com/p/json-path/ to
 * test some 'real-world' examples. json-path is licensed under
 * the Apache 2 license. http://www.apache.org/licenses/LICENSE-2.0
 */
package spray.json
package lenses

import org.specs2.mutable.Specification
import spray.json.{ JsValue, JsonParser, DefaultJsonProtocol }

class JsonPathExamplesSpec extends Specification with SpecHelpers {

  import JsonLenses._
  import DefaultJsonProtocol._

  val json = JsonParser(
    """
      |{ "store": {
      |    "book": [
      |      { "category": "reference",
      |        "author": "Nigel Rees",
      |        "title": "Sayings of the Century",
      |        "price": 8.95
      |      },
      |      { "category": "fiction",
      |        "author": "Evelyn Waugh",
      |        "title": "Sword of Honour",
      |        "price": 12.99,
      |        "isbn": "0-553-21311-3"
      |      }
      |    ],
      |    "bicycle": {
      |      "color": "red",
      |      "price": 19.95
      |    }
      |  }
      |}
    """.stripMargin)

  val singleElemArray = JsonParser(
    """
      |{ "store": {
      |    "book":
      |      { "category": "reference",
      |        "author": "Nigel Rees",
      |        "title": "Sayings of the Century",
      |        "price": 8.95
      |      }
      |    ,
      |    "bicycle": {
      |      "color": "red",
      |      "price": 19.95
      |    }
      |  }
      |}
    """.stripMargin)

  "Examples" should {
    "with Scala syntax" in {
      "All authors" in {
        json.extract[String](("store" / "book" / * / "author")) must be_==(Seq("Nigel Rees", "Evelyn Waugh"))
      }
      "Author of first book" in {
        json.extract[String](("store" / "book" / element(0) / "author")) must be_==("Nigel Rees")
      }
      "Author of first book no array" in {
        singleElemArray.extract[String](("store" / "book" / arrayOrSingletonAsArray / element(0) / "author")) must be_==("Nigel Rees")
      }
      "Books with category 'reference'" in {
        json.extract[String](("store" / "book" / filter("category".is[String](_ == "reference")) / "title")) must be_==(Seq("Sayings of the Century"))
      }
      "Books that cost more than 10 USD" in {
        json.extract[String](("store" / "book" / filter("price".is[Double](_ >= 10)) / "title")) must be_==(Seq("Sword of Honour"))
      }
      "All books that have isbn" in {
        json.extract[String](("store" / "book" / filter("isbn".is[JsValue](_ => true)) / "title")) must be_==(Seq("Sword of Honour"))
      }
      "Isbn of books that have isbn" in {
        json.extract[String](("store" / "book" / * / "isbn".?)) must be_==(Seq("0-553-21311-3"))
      }
      "All prices" in todo
    }
    "With Json-Path syntax" in {
      import JsonLenses.fromPath
      "All authors" in {
        json.extract[String](fromPath("$.store.book[*].author")) must be_==(Seq("Nigel Rees", "Evelyn Waugh"))
      }
      "Author of first book" in {
        json.extract[String](fromPath("$.store.book[0].author")) must be_==(Seq("Nigel Rees"))
      }
      "Books with category 'reference'" in {
        json.extract[String](fromPath("$.store.book[?(@.category == 'reference')].title")) must be_==(Seq("Sayings of the Century"))
      }
      "Books that cost more than 10 USD" in {
        json.extract[String](fromPath("$.store.book[?(@.price > 10)].title")) must be_==(Seq("Sword of Honour"))
      }
      "Books that cost more than 10 USD and with category as fiction" in {
        json.extract[String](fromPath("$.store.book[?(@.price > 10 && @.category == 'fiction')].title")) must be_==(Seq("Sword of Honour"))
      }
      "Books that cost more than 100 USD and with category as fiction" in {
        json.extract[String](fromPath("$.store.book[?(@.price > 100 && @.category == 'fiction')].title")) must be_==(Seq())
      }
      "Books that cost more than 10 USD or category reference" in {
        json.extract[String](fromPath("$.store.book[?(@.price > 10 || @.category == 'reference')].title")) must be_==(Seq("Sayings of the Century", "Sword of Honour"))
      }
      "Books that cost more than 10 USD or category reference" in {
        json.extract[String](fromPath("$.store.book[?(@.price < 1 || @.category == 'unknown-category')].title")) must be_==(Seq())
      }
      "All books that have isbn" in {
        json.extract[String](fromPath("$.store.book[?(@.isbn)].title")) must be_==(Seq("Sword of Honour"))
      }
      "Isbn of books that have isbn" in {
        val lens = fromPath("$.store.book[*].isbn")
        json.extract[String](lens) must be_==(Seq("0-553-21311-3"))

        val expected =
          JsonParser("""{"store":{"bicycle":{"color":"red","price":19.95},"book":[{"category":"reference","author":"Nigel Rees","title":"Sayings of the Century","price":8.95},{"category":"fiction","author":"Evelyn Waugh","title":"Sword of Honour","price":12.99,"isbn":"0-553-21311-3?"}]}}""")
        json.update(lens ! modify[String](_ + "?")) must be_==(expected)
      }
      "All prices" in todo
    }
  }
}
