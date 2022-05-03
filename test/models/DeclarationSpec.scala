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

import generators.ModelGenerators
import models.MessageType.{Amendment, Submission}
import models.Outcome.Accepted
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

import java.time.{Instant, LocalDate, ZoneOffset}

class DeclarationSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ModelGenerators
    with OptionValues {

  private val declarationEventGen =
    for {
      correlationId <- arbitrary[CorrelationId]
      declarationEvent <- arbitrary[DeclarationEvent]
    } yield (correlationId, declarationEvent)

  private val instantGen = {
    val minDate = LocalDate.of(2022, 7, 1)
    val maxDate = LocalDate.of(2999, 12, 31)

    Gen.choose(minDate, maxDate).map(_.atStartOfDay.atZone(ZoneOffset.UTC).toInstant)
  }

  private val declarationGen =
    for {
      eori <- arbitrary[String]
      lrn <- arbitrary[LocalReferenceNumber]
      declarationEvents <- Gen.mapOf(declarationEventGen)
      lastUpdated <- instantGen
    } yield Declaration(eori, lrn, Json.obj(), declarationEvents, lastUpdated)

  "must serialise and deserialise symmetrically" in {

    forAll(declarationGen) {
      declaration =>
        Json.toJson(declaration).validate[Declaration] mustEqual JsSuccess(declaration)
    }
  }

  ".withDeclarationEvent" - {

    "must set an event when there are none originally" in {

      val declaration = Declaration(
        eori = "123456789000",
        lrn = LocalReferenceNumber("ABC"),
        data = Json.obj("foo" -> "bar")
      )
      val correlationId = CorrelationId("1")
      val event = DeclarationEvent(Submission, None)

      val result = declaration.withDeclarationEvent(correlationId, event)

      result.declarationEvents.get(correlationId).value mustEqual event
    }

    "must add an event when there are already some events" in {

      val correlationId1 = CorrelationId("1")
      val event1 = DeclarationEvent(Submission, None)
      val correlationId2 = CorrelationId("2")
      val event2 = DeclarationEvent(Amendment, None)

      val declaration = Declaration(
        eori = "123456789000",
        lrn = LocalReferenceNumber("ABC"),
        data = Json.obj("foo" -> "bar"),
        declarationEvents = Map(
          correlationId1 -> event1
        )
      )

      val result = declaration.withDeclarationEvent(correlationId2, event2)

      result.declarationEvents mustEqual Map(
        correlationId1 -> event1,
        correlationId2 -> event2
      )
    }
  }

  ".withOutcome" - {

    "must set an outcome where the was an event with the same correlationId" in {

      val correlationId = CorrelationId("1")
      val event = DeclarationEvent(Submission, None)
      val declaration = Declaration(
        eori = "123456789000",
        lrn = LocalReferenceNumber("ABC"),
        data = Json.obj("foo" -> "bar"),
        declarationEvents = Map(correlationId -> event)
      )

      val outcome = Accepted(
        correlationId,
        Submission,
        Instant.now(),
        MovementReferenceNumber("111")
      )

      val result = declaration.withOutcome(outcome)

      result.right.get.declarationEvents.get(correlationId).value.outcome mustEqual Some(outcome)
    }

    "must return an error when a matching correlationId isn't found" in {
      val correlationId = CorrelationId("1")
      val event = DeclarationEvent(Submission, None)
      val declaration = Declaration(
        eori = "123456789000",
        lrn = LocalReferenceNumber("ABC"),
        data = Json.obj("foo" -> "bar"),
        declarationEvents = Map(correlationId -> event)
      )

      val outcome = Accepted(
        CorrelationId("2"),
        Submission,
        Instant.now(),
        MovementReferenceNumber("111")
      )

      val result = declaration.withOutcome(outcome)


      result mustEqual Left(DeclarationEventNotFound)
    }
  }
}
