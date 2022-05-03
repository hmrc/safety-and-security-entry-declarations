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

import play.api.libs.json.{JsObject, Json, OFormat}

case class SubmitDeclarationRequest(
                        data: JsObject,
                        declarationEvents: Option[Seq[RequestDeclarationEvent]]
                      ) {
  def toDeclaration(eori: String, lrn: LocalReferenceNumber): Declaration = {
    Declaration(
      eori,
      lrn,
      data,
      declarationEvents.getOrElse(Seq.empty).map(event => event.correlationId -> event.declarationEvent).toMap
    )
  }
}

case class RequestDeclarationEvent(correlationId: CorrelationId, declarationEvent: DeclarationEvent)

object RequestDeclarationEvent {
  implicit val formats: OFormat[RequestDeclarationEvent] = Json.format[RequestDeclarationEvent]
}

object SubmitDeclarationRequest {
  implicit val formats: OFormat[SubmitDeclarationRequest] = Json.format[SubmitDeclarationRequest]
}
