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

sealed trait MessageType

object MessageType {
  case object Submission extends MessageType
  case object Amendment extends MessageType

  implicit object MessageTypeWrites extends Writes[MessageType] {
    override def writes(mt: MessageType): JsValue = mt match {
      case Submission => JsString("submission")
      case Amendment => JsString("amendment")
    }
  }

  implicit object MessageTypeReads extends Reads[MessageType] {
    override def reads(v: JsValue): JsResult[MessageType] = v match {
      case JsString("submission") => JsSuccess(Submission)
      case JsString("amendment") => JsSuccess(Amendment)
      case s => JsError(s"Unexpected event type: '$s'")
    }
  }

}
