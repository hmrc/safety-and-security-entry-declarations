/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import play.api.libs.json._
import play.api.mvc.PathBindable

import scala.util.matching.Regex

final case class LocalReferenceNumber(value: String)

object LocalReferenceNumber {

  private val pattern: Regex = "([A-Za-z0-9]{1,22})".r.anchored

  private def fromString(input: String): Option[LocalReferenceNumber] = {
    input match {
      case pattern(lrn) => Some(LocalReferenceNumber(lrn))
      case _ => None
    }
  }

  implicit val reads: Reads[LocalReferenceNumber] = new Reads[LocalReferenceNumber] {

    override def reads(json: JsValue): JsResult[LocalReferenceNumber] =
      json match {
        case JsString(str) =>
          fromString(str)
            .map(JsSuccess(_))
            .getOrElse(JsError("Invalid Local Reference Number"))
        case _ =>
          JsError("Invalid Local Reference Number")
      }
  }

  implicit val writes: Writes[LocalReferenceNumber] = new Writes[LocalReferenceNumber] {

    override def writes(o: LocalReferenceNumber): JsValue =
      JsString(o.value)
  }

  implicit val pathBindable: PathBindable[LocalReferenceNumber] =
    new PathBindable[LocalReferenceNumber] {

      override def bind(key: String, value: String): Either[String, LocalReferenceNumber] =
        fromString(value) match {
          case Some(lrn) => Right(lrn)
          case None => Left("Invalid Local Reference Number")
        }

      override def unbind(key: String, value: LocalReferenceNumber): String =
        value.value
    }
}
