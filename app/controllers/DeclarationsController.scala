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

package controllers


import controllers.actions.IdentifierAction
import models.{APIError, InvalidRequestBody, SubmitDeclarationRequest}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import services.DeclarationsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationsController @Inject()(
                                          cc: ControllerComponents,
                                          declarationsService: DeclarationsService,
                                          authAction: IdentifierAction,
                                          implicit val ec: ExecutionContext
                                        )
  extends BackendController(cc) {

  def submitDeclaration(eori: String, lrn: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    authAction.withAuthAndEnrolmentCheck[JsValue](eori){ _ =>
      request.body.validate[SubmitDeclarationRequest].fold(_ => {
        Future.successful(Status(InvalidRequestBody.httpCode)(Json.toJson(InvalidRequestBody: APIError)))
      }, requestModel => {
          declarationsService.submitDeclaration(requestModel, eori, lrn).map {
            case Right(_) => Created
            case Left(error) => Status(error.httpCode)(Json.toJson(error))
          }
      })
    }
  }
}
