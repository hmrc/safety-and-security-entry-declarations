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


import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.{CorrelationId, Declaration, DeclarationEvent, LocalReferenceNumber, MessageType, SaveDeclarationEventRequest, SubmitDeclarationRequest}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.test.Injecting
import repositories.DeclarationRepository
import support.{IntegrationBaseSpec, WiremockHelper}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class EventsControllerISpec extends IntegrationBaseSpec with Injecting with DefaultPlayMongoRepositorySupport[Declaration] with WiremockHelper {

  lazy val ws: WSClient = app.injector.instanceOf(classOf[WSClient])

  override val repository: DeclarationRepository = app.injector.instanceOf[DeclarationRepository]


  val ssEnrolment: String => JsObject = eori => Json.obj(
    "key" -> "HMRC-SS-ORG",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "EORI",
        "value" -> s"$eori"
      )
    )
  )

  private def successfulAuthResponse(enrolments: JsObject*): JsObject = {
    Json.obj("allEnrolments" -> enrolments)
  }

  def successfulAuthMock(): StubMapping = {
    wireMockServer.stubFor(
      post(urlMatching("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(successfulAuthResponse().toString)
            .withHeader("Content-type", "Application/json")
        )
    )
  }

  def failingAuthMock(): StubMapping = {
    wireMockServer.stubFor(
      post(urlMatching("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withBody(successfulAuthResponse().toString)
            .withHeader("Content-type", "Application/json")
            .withHeader("Failing-Enrolment", "HMRC-SS-ORG")
        )
    )
  }
  val successRequestBodySubmit: SubmitDeclarationRequest = SubmitDeclarationRequest(
    JsObject(Seq("test"->JsString("string"))),
    None
  )

  val successRequestBodySaveEvent: SaveDeclarationEventRequest = SaveDeclarationEventRequest(
    CorrelationId("Correlation1"),
    MessageType.Amendment,
    None
  )

  val eori = "GB205672212000"
  val lrn = "LocalReference1"

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(deleteAll())
  }


  "save declaration event" must {
    "add an event to a declaration" in {

      successfulAuthMock()

      await(insert(successRequestBodySubmit.toDeclaration(eori, LocalReferenceNumber(lrn))))

      val result = await(buildRequest(s"/safety-and-security-entry-declarations/declaration/$eori/$lrn/event")
        .withHttpHeaders("Content-Type" -> "application/json", "Authorization" -> s"Bearer dummyBearer")
        .post[JsValue](Json.toJson(successRequestBodySaveEvent)))

      result.status mustBe 201
      val repositoryRecord = await(repository.get("GB205672212000", LocalReferenceNumber("LocalReference1")))

      repositoryRecord.get.declarationEvents.get(CorrelationId("Correlation1")) mustBe Some(DeclarationEvent(MessageType.Amendment, None))
      repositoryRecord.get.lrn mustBe LocalReferenceNumber(lrn)
      repositoryRecord.get.eori mustBe eori
    }

    "return 404 when trying to add an event for non-existing declaration" in {

      successfulAuthMock()

      val result = await(buildRequest(s"/safety-and-security-entry-declarations/declaration/$eori/$lrn/event")
        .withHttpHeaders("Content-Type" -> "application/json", "Authorization" -> s"Bearer dummyBearer")
        .post[JsValue](Json.toJson(successRequestBodySaveEvent)))

      result.status mustBe 404

      result.body mustBe "{\"code\":\"DECLARATION_NOT_FOUND\",\"message\":\"The request tried to update a record that doesn't exist\"}"
    }


    "return 401 if the user doesn't have the required enrolments" in {

      failingAuthMock()

      val result = await(buildRequest(s"/safety-and-security-entry-declarations/declaration/$eori/$lrn/event")
        .withHttpHeaders("Content-Type" -> "application/json", "Authorization" -> "Bearer dummyBearer")
        .post[JsValue](Json.toJson(successRequestBodySaveEvent)))

      result.status mustBe 401

      result.body mustBe "{\"code\":\"MISSING_SS_ENROLMENT\",\"message\":\"The consumer does not have the required authorisation to make this request\"}"
    }
  }
}