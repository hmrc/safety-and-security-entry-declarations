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

package controllers.actions

import com.google.inject.Inject
import config.AppConfig
import models.{APIError, MissingSSEnrolment, Unauthorised}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class IdentifierAction @Inject() (
                                                override val authConnector: AuthConnector,
                                                config: AppConfig,
                                                val parser: BodyParsers.Default
                                              )(implicit val executionContext: ExecutionContext)
  extends AuthorisedFunctions {

  def withAuthAndEnrolmentCheck[A](eori: String)(block: Request[A] => Future[Result])(implicit request: Request[A]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(Enrolment(config.enrolment)
      .withIdentifier(config.enrolment, eori) and
      CredentialStrength(CredentialStrength.strong) and
      AffinityGroup.Organisation
    )(block(request)) recover {
      case _: AuthorisationException =>
        Status(MissingSSEnrolment.httpCode)(Json.toJson(MissingSSEnrolment: APIError))
      case _: Exception =>
        Status(Unauthorised.httpCode)(Json.toJson(Unauthorised: APIError))
    }
  }
}