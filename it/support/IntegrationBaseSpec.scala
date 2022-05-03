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

package support

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import play.api.{Application}
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext

trait IntegrationBaseSpec
  extends PlaySpec
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with FutureAwaits
    with DefaultAwaitTimeout
    with MongoSupport {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  lazy val client: WSClient = app.injector.instanceOf[WSClient]

  def servicesConfig: Map[String, Any] =
    Map(
      "metrics.enabled"                                 -> false,
      "auditing.enabled"                                -> false,
      "microservice.services.auth.host"                 -> "localhost",
      "microservice.services.auth.port"                 -> 11111

    )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(servicesConfig)
    .build()


  def buildRequest[T](path: String): WSRequest = client.url(s"http://localhost:$port$path").withFollowRedirects(false)

}