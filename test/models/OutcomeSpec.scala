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
import models.Outcome.{Accepted, Rejected}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsSuccess, Json}

class OutcomeSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ModelGenerators {

  "must serialise and deserialise symmetrically as Accepted" in {

    forAll(arbitrary[Accepted]) {
      accepted =>
        Json.toJson(accepted: Outcome).validate[Outcome] mustEqual JsSuccess(accepted)
    }
  }

  "must serialise and deserialise symmetrically as Rejected" in {

    forAll(arbitrary[Rejected]) {
      rejected =>
        Json.toJson(rejected: Outcome).validate[Outcome] mustEqual JsSuccess(rejected)
    }
  }

  "must not read from invalid objects" in {

    forAll(arbitrary[String]) {
      string =>
        Json.toJson(string).validate[Outcome] mustBe a[JsError]
    }
  }
}
