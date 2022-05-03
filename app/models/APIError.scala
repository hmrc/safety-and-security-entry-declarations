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

import play.api.libs.json.{JsPath, Writes}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, _}

sealed class APIError(val httpCode: Int, val errorCode: String, val message: String)

case object InvalidRequestBody extends APIError(400, "INVALID_REQUEST", "The request body did not match the format expected")

case object InvalidLocalReferenceNumber extends APIError(400, "INVALID_LRN", "The provided LocalReferenceNumber did not match the expected format")

case object UnknownErrorInsertingRecord extends APIError(503, "UNKNOWN_ERROR", "An unexpected error occurred trying to insert the given document")

case object MissingSSEnrolment extends APIError(401, "MISSING_SS_ENROLMENT", "The consumer does not have the required authorisation to make this request")

case object Unauthorised extends APIError(401, "UNAUTHORISED", "The consumer does not have the required authorisation to make this request")

case object DeclarationNotfound extends APIError(404, "DECLARATION_NOT_FOUND", "The request tried to update a record that doesn't exist")

case object DeclarationEventNotFound extends APIError(404, "DECLARATION_EVENT_NOT_FOUND", "The request tried to update a record that doesn't exist")


object APIError {
  implicit val writes: Writes[APIError] = (
    (JsPath \ "code").write[String] and
      (JsPath \ "message").write[String]
    ) (model => (model.errorCode, model.message))
}