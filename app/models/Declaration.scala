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
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class Declaration(
  eori: String,
  lrn: LocalReferenceNumber,
  data: JsObject,
  declarationEvents: Map[CorrelationId, DeclarationEvent] = Map.empty,
  lastUpdated: Instant = Instant.now
) {

  def withDeclarationEvent(corrId: CorrelationId, event: DeclarationEvent): Declaration = copy(
    declarationEvents = declarationEvents + (corrId -> event),
    lastUpdated = Instant.now()
  )

  def withOutcome(outcome: Outcome): Either[APIError, Declaration] = declarationEvents.get(outcome.correlationId).map {
      event =>
        Right(copy(
          declarationEvents = declarationEvents.updated(outcome.correlationId, event.copy(outcome = Some(outcome))),
          lastUpdated = Instant.now())
        )
  }.getOrElse(Left(DeclarationEventNotFound))

}

object Declaration {
  implicit val declarationEventsReads: Reads[Map[CorrelationId, DeclarationEvent]] =
    new Reads[Map[CorrelationId, DeclarationEvent]] {
      override def reads(v: JsValue): JsResult[Map[CorrelationId, DeclarationEvent]] = JsSuccess(
        v.as[Map[String, JsValue]].map {
          case (k, v) => CorrelationId(k) -> v.as[DeclarationEvent]
        }
      )
    }

  implicit val declarationEventsWrites: Writes[Map[CorrelationId, DeclarationEvent]] =
    new Writes[Map[CorrelationId, DeclarationEvent]] {
      override def writes(m: Map[CorrelationId, DeclarationEvent]): JsValue = JsObject(
        m.map { case (k, v) => k.id -> Json.toJson(v) }
      )
    }

  val reads: Reads[Declaration] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "eori").read[String] and
      (__ \ "lrn").read[LocalReferenceNumber] and
      (__ \ "data").read[JsObject] and
      (__ \ "declarationEvents").read[Map[CorrelationId, DeclarationEvent]] and
      (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(Declaration.apply _)
  }

  val writes: OWrites[Declaration] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "eori").write[String] and
      (__ \ "lrn").write[LocalReferenceNumber] and
      (__ \ "data").write[JsObject] and
      (__ \ "declarationEvents").write[Map[CorrelationId, DeclarationEvent]] and
      (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(unlift(Declaration.unapply))
  }

  implicit val format: OFormat[Declaration] = OFormat(reads, writes)
}
