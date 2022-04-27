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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

import java.time.{LocalDate, ZoneOffset}

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

    "must return the same Declaration with a new event added to the map" in {

      forAll(declarationGen, arbitrary[CorrelationId], arbitrary[DeclarationEvent]) {
        case (declaration, correlationId, newEvent) =>

          val result = declaration.withDeclarationEvent(correlationId, newEvent)

          result.declarationEvents.get(correlationId).value mustEqual newEvent
          result.declarationEvents - correlationId mustEqual declaration.declarationEvents
      }
    }
  }
}
