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

import models.MessageType.Submission
import models.Outcome.Accepted
import models.{CorrelationId, Declaration, DeclarationEvent, DeclarationNotfound, InvalidLocalReferenceNumber, LocalReferenceNumber, MessageType, MovementReferenceNumber, Outcome, SaveDeclarationEventRequest, SubmitDeclarationRequest}
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, JsString}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import repositories.DeclarationRepository

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationsServiceSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with FutureAwaits
    with DefaultAwaitTimeout {



  val mockRepo = mock[DeclarationRepository]


  val service = new DeclarationsService(mockRepo)
  ".submitDeclaration" - {
    "return a Right(()) when the repository indicates the submission succeeded" in {
      when(mockRepo.upsert(any[Declaration]))
        .thenReturn(Future(Right(())))

      val eori = "GB205672212000"
      val lrn = LocalReferenceNumber("1")
      val request = SubmitDeclarationRequest(JsObject(Seq("test" -> JsString("test"))), None)

      val result = await(service.submitDeclaration(request, eori, lrn.value))

      result mustBe Right(())
    }

    "return an invalid local reference number error if the LRN doesn't match the required regex" in {
      val eori = "GB205672212000"
      val request = SubmitDeclarationRequest(JsObject(Seq("test" -> JsString("test"))), None)

      val result = await(service.submitDeclaration(request, eori, "DOESN'T MATCH"))

      result mustBe Left(InvalidLocalReferenceNumber)
    }

    "propagate an error passed back from the repository" in {
      when(mockRepo.upsert(any[Declaration]))
        .thenReturn(Future(Left(DeclarationNotfound)))

      val eori = "GB205672212000"
      val lrn = LocalReferenceNumber("1")
      val request = SubmitDeclarationRequest(JsObject(Seq("test" -> JsString("test"))), None)

      val result = await(service.submitDeclaration(request, eori, lrn.value))

      result mustBe Left(DeclarationNotfound)
    }
  }

  ".getDeclarations" - {
    "return an empty list of declarations from the repository" in {
      when(mockRepo.get(any[String]))
        .thenReturn(Future(Seq.empty))

      val eori = "GB205672212000"

      val result = await(service.getDeclarations(eori))

      result mustBe Seq.empty
    }

    "return a populated list of declarations from the repository" in {
      val eori = "GB205672212000"

      val declarations = Seq(
        Declaration(eori, LocalReferenceNumber("1"), JsObject(Seq("test"->JsString("Value")))),
        Declaration(eori, LocalReferenceNumber("2"), JsObject(Seq("tes1"->JsString("Value1"))))
      )
      when(mockRepo.get(any[String]))
        .thenReturn(Future(declarations))


      val result = await(service.getDeclarations(eori))

      result mustBe declarations
    }
  }

  ".getDeclaration" - {
    "return a declaration when the repository returns one" in {
      val eori = "GB205672212000"
      val lrn = LocalReferenceNumber("1")
      val data = JsObject(Seq("test"->JsString("Value")))
      val declaration = Declaration(eori, lrn, data)

      when(mockRepo.get(any[String], any[LocalReferenceNumber]))
        .thenReturn(Future(Some(declaration)))

      val result = await(service.getDeclaration(eori, lrn.value))

      result mustBe Right(declaration)
    }

    "return an invalid local reference number error if the LRN doesn't match the required regex" in {
      val eori = "GB205672212000"

      val result = await(service.getDeclaration(eori, "DOESN'T MATCH"))

      result mustBe Left(InvalidLocalReferenceNumber)
    }

    "propagate an error if the repository couldn't find a declaration" in {
      when(mockRepo.get(any[String], any[LocalReferenceNumber]))
        .thenReturn(Future(None))

      val eori = "GB205672212000"
      val lrn = LocalReferenceNumber("1")

      val result = await(service.getDeclaration(eori, lrn.value))

      result mustBe Left(DeclarationNotfound)
    }
  }

  ".setOutcome" - {
    "return a Right(()) when the repository indicates setting the outcome succeeded" in {
      when(mockRepo.setOutcome(any[String], any[LocalReferenceNumber], any[Outcome]))
        .thenReturn(Future(Right(())))

      val eori = "GB205672212000"
      val lrn = LocalReferenceNumber("1")
      val outcome = Accepted(CorrelationId("1"), MessageType.Submission, Instant.now(), MovementReferenceNumber("1"))

      val result = await(service.setOutcome(eori, lrn.value, outcome))

      result mustBe Right(())
    }

    "return an invalid local reference number error if the LRN doesn't match the required regex" in {
      val eori = "GB205672212000"
      val outcome = Accepted(CorrelationId("1"), MessageType.Submission, Instant.now(), MovementReferenceNumber("1"))

      val result = await(service.setOutcome(eori, "DOESN'T MATCH", outcome))

      result mustBe Left(InvalidLocalReferenceNumber)
    }

    "propagate an error passed back from the repository" in {
      when(mockRepo.setOutcome(any[String], any[LocalReferenceNumber], any[Outcome]))
        .thenReturn(Future(Left(DeclarationNotfound)))

      val eori = "GB205672212000"
      val lrn = LocalReferenceNumber("1")
      val outcome = Accepted(CorrelationId("1"), MessageType.Submission, Instant.now(), MovementReferenceNumber("1"))

      val result = await(service.setOutcome(eori, lrn.value, outcome))

      result mustBe Left(DeclarationNotfound)
    }
  }

  ".saveDeclarationEvent" - {
    "return a Right(()) when the repository indicates saving the event succeeded" in {
      when(mockRepo.insertEvent(any[String], any[LocalReferenceNumber], any[CorrelationId], any[DeclarationEvent]))
        .thenReturn(Future(Right(())))

      val eori = "GB205672212000"
      val lrn = LocalReferenceNumber("1")
      val eventRequest = SaveDeclarationEventRequest(CorrelationId("1"), MessageType.Submission, None)

      val result = await(service.saveDeclarationEvent(eventRequest, eori, lrn.value))

      result mustBe Right(())
    }

    "return an invalid local reference number error if the LRN doesn't match the required regex" in {
      val eori = "GB205672212000"

      val eventRequest = SaveDeclarationEventRequest(CorrelationId("1"), MessageType.Submission, None)

      val result = await(service.saveDeclarationEvent(eventRequest, eori, "DOESN'T MATCH"))

      result mustBe Left(InvalidLocalReferenceNumber)
    }

    "propagate an error passed back from the repository" in {
      when(mockRepo.insertEvent(any[String], any[LocalReferenceNumber], any[CorrelationId], any[DeclarationEvent]))
        .thenReturn(Future(Left(DeclarationNotfound)))

      val eori = "GB205672212000"
      val lrn = LocalReferenceNumber("1")
      val outcome = Accepted(CorrelationId("1"), MessageType.Submission, Instant.now(), MovementReferenceNumber("1"))

      val result = await(service.setOutcome(eori, lrn.value, outcome))

      result mustBe Left(DeclarationNotfound)
    }
  }
}
