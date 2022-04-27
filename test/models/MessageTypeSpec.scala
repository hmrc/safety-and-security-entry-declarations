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

import models.MessageType.{Amendment, Submission}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class MessageTypeSpec extends AnyFreeSpec with Matchers {

  "MessageType" - {

    "must serialise and deserialise as a Submission" in {

      val json = Json.toJson(Submission: MessageType)
      json.validate[MessageType] mustEqual JsSuccess(Submission)
    }

    "must serialise and deserialise as an Amendment" in {

      val json = Json.toJson(Amendment: MessageType)
      json.validate[MessageType] mustEqual JsSuccess(Amendment)
    }

    "must not read from invalid Json" in {
      JsString("foo").validate[MessageType] mustBe a[JsError]
    }
  }
}
