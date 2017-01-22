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

import java.lang.StringBuilder

import org.parboiled.Context
import org.parboiled.scala._
import org.parboiled.errors.{ ErrorUtils, ParsingException }

/**
 * A parser for json-path expression as specified here:
 * [[http://goessner.net/articles/JsonPath/]]
 */
object JsonPathParser extends Parser with BasicRules {
  def JsonPathExpr = rule { Path ~ EOI }

  def Path: Rule1[JsonPath.Path] = rule { Root ~ OptionalSelection }

  def Root: Rule1[JsonPath.Root.type] = rule {
    // we don't distinguish between '$' and '@'
    anyOf("$@") ~ push(JsonPath.Root)
  }

  def OptionalSelection: ReductionRule1[JsonPath.Path, JsonPath.Path] = rule {
    Projection ~~> JsonPath.Selection ~ OptionalSelection |
      EMPTY ~~> identity
  }

  def Projection: Rule1[JsonPath.Projection] = rule {
    "." ~ DotProjection |
      "[" ~ BracketProjection ~ "]"
  }

  def DotProjection: Rule1[JsonPath.Projection] = rule {
    ByFieldName
  }
  def AllElements = rule { "*" ~ push(JsonPath.AllElements) }
  def ByFieldName = rule { FieldName ~~> JsonPath.ByField }

  def BracketProjection: Rule1[JsonPath.Projection] = rule {
    Digits ~> (d ⇒ JsonPath.ByIndex(d.toInt)) |
      SingleQuotedString ~~> JsonPath.ByField |
      AllElements |
      "?(" ~ WhiteSpace ~ Predicate ~ WhiteSpace ~ ")" ~~> JsonPath.ByPredicate
  }

  def Predicate: Rule1[JsonPath.Predicate] = rule {
    Lt | Gt | Eq | Exists
  }
  def Eq: Rule1[JsonPath.Eq] = rule { op("==")(JsonPath.Eq) }
  def Lt: Rule1[JsonPath.Lt] = rule { op("<")(JsonPath.Lt) }
  def Gt: Rule1[JsonPath.Gt] = rule { op(">")(JsonPath.Gt) }
  def Exists: Rule1[JsonPath.Exists] = rule {
    Path ~~> JsonPath.Exists
  }

  def op[T](op: String)(cons: (JsonPath.Expr, JsonPath.SimpleExpr) ⇒ T) =
    Expr ~ WhiteSpace ~ op ~ WhiteSpace ~ SimpleExpr ~~> cons

  def Expr: Rule1[JsonPath.Expr] = rule {
    Path ~~> JsonPath.PathExpr |
      SimpleExpr
  }
  def SimpleExpr: Rule1[JsonPath.SimpleExpr] = rule {
    JsConstant ~~> JsonPath.Constant
  }
  def JsConstant: Rule1[JsValue] = rule {
    JsonNumber |
      SingleQuotedString ~~> (JsString(_))
  }

  val WhiteSpaceChars = " \n\r\t\f"
  def FieldName: Rule1[String] = rule {
    oneOrMore("a" - "z" | "A" - "Z" | "0" - "9" | anyOf("_-")) ~> identity
  }

  def SingleQuotedString: Rule1[String] =
    rule { "'" ~ push(new java.lang.StringBuilder) ~ zeroOrMore(!anyOf("'") ~ ("\\" ~ EscapedChar | NormalChar)) } ~ "'" ~~> (_.toString)

  /**
   * The main parsing method. Uses a ReportingParseRunner (which only reports the first error) for simplicity.
   */
  def apply(path: String): JsonPath.Path = apply(path.toCharArray)

  /**
   * The main parsing method. Uses a ReportingParseRunner (which only reports the first error) for simplicity.
   */
  def apply(path: Array[Char]): JsonPath.Path = {
    val parsingResult = ReportingParseRunner(JsonPathExpr).run(path)
    parsingResult.result.getOrElse {
      throw new ParsingException("Invalid JSON source:\n" + ErrorUtils.printParseErrors(parsingResult))
    }
  }
}

// a set of basic rules taken from the old spray-json parser
// see https://github.com/spray/spray-json/blob/v1.2.6/src/main/scala/spray/json/JsonParser.scala
trait BasicRules { _: Parser ⇒
  def EscapedChar = rule(
    anyOf("\"\\/") ~:% withContext(appendToSb(_)(_))
      | "b" ~ appendToSb('\b')
      | "f" ~ appendToSb('\f')
      | "n" ~ appendToSb('\n')
      | "r" ~ appendToSb('\r')
      | "t" ~ appendToSb('\t')
      | Unicode ~~% withContext((code, ctx) ⇒ appendToSb(code.asInstanceOf[Char])(ctx)))

  def NormalChar = rule { !anyOf("\"\\") ~ ANY ~:% (withContext(appendToSb(_)(_))) }
  def Unicode = rule { "u" ~ group(HexDigit ~ HexDigit ~ HexDigit ~ HexDigit) ~> (java.lang.Integer.parseInt(_, 16)) }

  def JsonNumber = rule { group(Integer ~ optional(Frac) ~ optional(Exp)) ~> (JsNumber(_)) ~ WhiteSpace }
  def Frac = rule { "." ~ Digits }
  def Exp = rule { ignoreCase("e") ~ optional(anyOf("+-")) ~ Digits }

  def Integer = rule { optional("-") ~ (("1" - "9") ~ Digits | Digit) }
  def Digits = rule { oneOrMore(Digit) }
  def Digit = rule { "0" - "9" }
  def HexDigit = rule { "0" - "9" | "a" - "f" | "A" - "F" }
  def WhiteSpace: Rule0 = rule { zeroOrMore(anyOf(" \n\r\t\f")) }

  def appendToSb(c: Char): Context[Any] ⇒ Unit = { ctx ⇒
    ctx.getValueStack.peek.asInstanceOf[StringBuilder].append(c)
    ()
  }
}