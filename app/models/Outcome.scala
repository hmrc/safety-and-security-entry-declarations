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

import java.time.Instant

sealed trait Outcome {
  val correlationId: CorrelationId
  val messageType: MessageType
  val timestamp: Instant
}

object Outcome {
  case class Accepted(
                       correlationId: CorrelationId,
                       messageType: MessageType,
                       timestamp: Instant,
                       mrn: MovementReferenceNumber
                     ) extends Outcome

  case class Rejected(
                       correlationId: CorrelationId,
                       messageType: MessageType,
                       timestamp: Instant,
                       reason: RejectionReason
                     ) extends Outcome

  implicit val acceptedFormat = Json.format[Accepted]
  implicit val rejectedFormat = Json.format[Rejected]
  implicit val outcomeReads = acceptedFormat.widen[Outcome] orElse rejectedFormat.widen[Outcome]
  implicit val outcomeWrites: Writes[Outcome] = Writes {
    case o: Accepted => Json.toJson(o)
    case o: Rejected => Json.toJson(o)
  }
}
