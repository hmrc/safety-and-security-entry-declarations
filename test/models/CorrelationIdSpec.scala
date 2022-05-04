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
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNumber, JsString, JsSuccess, Json}

class CorrelationIdSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ModelGenerators
    with EitherValues {


  "CorrelationId" - {

    "must serialise and deserialise symmetrically to/from a JsString of the inner value" in {

      forAll(arbitrary[CorrelationId]) { correlationId =>

        Json.toJson(correlationId) mustEqual JsString(correlationId.id)
        JsString(correlationId.id).validate[CorrelationId] mustEqual JsSuccess(correlationId)
      }
    }

    "must not read from a JsObject or JsNumber" in {

      JsNumber(123).validate[CorrelationId] mustBe a[JsError]
      Json.obj("lrn" -> "abc").validate[CorrelationId] mustBe a[JsError]
    }
  }
}
