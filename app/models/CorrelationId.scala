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

case class CorrelationId(id: String)

object CorrelationId {

  implicit val reads: Reads[CorrelationId] = new Reads[CorrelationId] {

    override def reads(json: JsValue): JsResult[CorrelationId] =
      json match {
        case JsString(str) =>
          JsSuccess(CorrelationId(str))
        case _ =>
          JsError("Invalid correlationId")
      }
  }

  implicit val writes: Writes[CorrelationId] = new Writes[CorrelationId] {

    override def writes(o: CorrelationId): JsValue =
      JsString(o.id)
  }
}