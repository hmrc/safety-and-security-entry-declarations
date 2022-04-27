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
import play.api.mvc.PathBindable

class LocalReferenceNumberSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ModelGenerators
    with EitherValues {


  private val pathBindable = implicitly[PathBindable[LocalReferenceNumber]]

  "Local Reference Number" - {

    "must bind and unbind valid values from/to a URL" in {

      forAll(arbitrary[LocalReferenceNumber]) { lrn =>

        pathBindable.bind("key", lrn.value).value mustEqual lrn
        pathBindable.unbind("key", lrn) mustEqual lrn.value
      }
    }

    "must not bind invalid values from a URL" in {

      pathBindable
        .bind("key", "invalid value")
        .left
        .value mustEqual "Invalid Local Reference Number"
    }

    "must serialise and deserialise to/from a JsString of the inner value" in {

      forAll(arbitrary[LocalReferenceNumber]) { lrn =>

        Json.toJson(lrn) mustEqual JsString(lrn.value)
        JsString(lrn.value).validate[LocalReferenceNumber] mustEqual JsSuccess(lrn)
      }
    }

    "must not read from a JsString with an invalid value" in {

      JsString("invalid value").validate[LocalReferenceNumber] mustBe a[JsError]
    }

    "must not read from a JsObject or JsNumber" in {

      JsNumber(123).validate[LocalReferenceNumber] mustBe a[JsError]
      Json.obj("lrn" -> "abc").validate[LocalReferenceNumber] mustBe a[JsError]
    }
  }
}
