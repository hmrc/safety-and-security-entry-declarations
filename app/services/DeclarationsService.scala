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

package services

import models.{APIError, CorrelationId, Declaration, DeclarationEvent, DeclarationNotfound, InvalidLocalReferenceNumber, LocalReferenceNumber, Outcome, SaveDeclarationEventRequest, SubmitDeclarationRequest}
import repositories.DeclarationRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationsService @Inject()(
                                     declarationRepository: DeclarationRepository
                                   ){
  def submitDeclaration(submitDeclarationRequest: SubmitDeclarationRequest, eori: String, lrn: String)(implicit ec: ExecutionContext): Future[Either[APIError, Unit]] = {
    LocalReferenceNumber.fromString(lrn).fold[Future[Either[APIError, Unit]]](
      Future(Left(InvalidLocalReferenceNumber))
    )(localReferenceNumber =>
      declarationRepository.upsert(submitDeclarationRequest.toDeclaration(eori,localReferenceNumber))
    )
  }

  def getDeclarations(eori: String)(implicit ec: ExecutionContext): Future[Seq[Declaration]] = {
    declarationRepository.get(eori)
  }

  def getDeclaration(eori: String, lrn: String)(implicit ec: ExecutionContext): Future[Either[APIError, Declaration]] = {
    LocalReferenceNumber.fromString(lrn).fold[Future[Either[APIError, Declaration]]](
      Future(Left(InvalidLocalReferenceNumber))
    )(localReferenceNumber =>
      declarationRepository.get(eori, localReferenceNumber).map(_.fold[Either[APIError, Declaration]](Left(DeclarationNotfound))(Right(_)))
    )
  }

  def setOutcome(eori: String, lrn: String, outcome: Outcome)(implicit ec: ExecutionContext): Future[Either[APIError, Unit]] = {
    LocalReferenceNumber.fromString(lrn).fold[Future[Either[APIError, Unit]]](
      Future(Left(InvalidLocalReferenceNumber))
    )(localReferenceNumber =>
      declarationRepository.setOutcome(eori, localReferenceNumber, outcome)
    )
  }

  def saveDeclarationEvent(saveDeclarationEventRequest: SaveDeclarationEventRequest, eori: String, lrn: String)(implicit ec: ExecutionContext): Future[Either[APIError, Unit]] = {
    LocalReferenceNumber.fromString(lrn).fold[Future[Either[APIError, Unit]]](
      Future(Left(InvalidLocalReferenceNumber))
    )(localReferenceNumber =>
      declarationRepository.insertEvent(eori, localReferenceNumber, saveDeclarationEventRequest.correlationId, DeclarationEvent(saveDeclarationEventRequest.messageType, saveDeclarationEventRequest.outcome))
    )
  }
}
