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

package repositories

import models.{CorrelationId, Declaration, DeclarationEvent, DeclarationNotfound, LocalReferenceNumber, MessageType}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, JsString}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class DeclarationRepositoryISpec extends PlaySpec with FutureAwaits with DefaultAwaitTimeout with DefaultPlayMongoRepositorySupport[Declaration] {

  implicit val ec = scala.concurrent.ExecutionContext.global
  override val repository = new DeclarationRepository(mongoComponent)

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(deleteAll())
  }

  val declaration = Declaration(
    "GB205672212000",
    LocalReferenceNumber("LocalReference1"),
    JsObject(Seq("test"->JsString("string"))),
    Map.empty
  )

  ".upsert" should {
    "Insert a new declaration" in {
      val result = await(repository.upsert(declaration))

      result mustBe Right(())
    }

    "Insert a new declaration with an already used LRN but different EORI" in {
      val preTestInsert = await(repository.upsert(declaration.copy(eori = "GB205672212001")))
      preTestInsert mustBe Right(())

      val result = await(repository.upsert(declaration))

      result mustBe Right(())
    }

    "Insert a new declaration with an already used EORI but different LRN" in {
      val preTestInsert = await(repository.upsert(declaration.copy(lrn = LocalReferenceNumber("LocalReference2"))))
      preTestInsert mustBe Right(())

      val result = await(repository.upsert(declaration))

      result mustBe Right(())
    }

    "Update a declaration for an LRN & EORI that already exists" in {
      val preTestInsert = await(repository.upsert(declaration))
      preTestInsert mustBe Right(())

      val result = await(repository.upsert(declaration.copy(data = JsObject(Seq("updatedTest" -> JsString("updatedString"))))))

      result mustBe Right(())

      val dbRecord = await(repository.get(declaration.eori, declaration.lrn))
      dbRecord.get.data mustBe JsObject(Seq("updatedTest" -> JsString("updatedString")))
    }
  }

  ".insertEvent" should {
    "Add an event to an existing declaration" in {
      val preTestSetup = await(repository.upsert(declaration))
      preTestSetup mustBe Right(())

      val result = await(repository.insertEvent("GB205672212000", LocalReferenceNumber("LocalReference1"), CorrelationId("correlation1"), DeclarationEvent(MessageType.Amendment, None)))

      result mustBe Right()

      val dbDocument = await(repository.get("GB205672212000", LocalReferenceNumber("LocalReference1")))

      dbDocument.get.declarationEvents.head mustBe (CorrelationId("correlation1")-> DeclarationEvent(MessageType.Amendment, None))
    }

    "Add an event to an existing declaration that already had an event" in {
      val preTestSetup = await(repository.upsert(declaration.copy(declarationEvents =  Map(CorrelationId("correlation1") -> DeclarationEvent(MessageType.Amendment, None)))))
      preTestSetup mustBe Right(())

      val result = await(repository.insertEvent("GB205672212000", LocalReferenceNumber("LocalReference1"), CorrelationId("correlation2"), DeclarationEvent(MessageType.Submission, None)))

      result mustBe Right()

      val dbDocument = await(repository.get("GB205672212000", LocalReferenceNumber("LocalReference1")))

      dbDocument.get.declarationEvents.get(CorrelationId("correlation2")) mustBe Some(DeclarationEvent(MessageType.Submission, None))
    }

    "Return an error if the specified declaration doesn't exist" in {
      val result = await(repository.insertEvent("GB205672212000", LocalReferenceNumber("LocalReference1"), CorrelationId("correlation2"), DeclarationEvent(MessageType.Submission, None)))

      result mustBe Left(DeclarationNotfound)
    }

  }

  ".get (by EORI)" should {
    "return a list containing a single declaration if it's the only one stored that matches the input EORI" in {
      val preTestSetup1 = await(repository.upsert(declaration))
      val preTestSetup2 = await(repository.upsert(declaration.copy(eori = "GB205672212001")))
      preTestSetup1 mustBe Right(())
      preTestSetup2 mustBe Right(())

      val result = await(repository.get("GB205672212000"))

      result mustBe Seq(declaration)
    }

    "return a list containing multiple declarations that match the input EORI" in {
      val preTestSetup1 = await(repository.upsert(declaration))
      val preTestSetup2 = await(repository.upsert(declaration.copy(eori = "GB205672212001")))
      val preTestSetup3 = await(repository.upsert(declaration.copy(lrn = LocalReferenceNumber("LocalReference2"))))
      val preTestSetup4 = await(repository.upsert(declaration.copy(lrn = LocalReferenceNumber("LocalReference3"))))
      preTestSetup1 mustBe Right(())
      preTestSetup2 mustBe Right(())
      preTestSetup3 mustBe Right(())
      preTestSetup4 mustBe Right(())

      val result = await(repository.get("GB205672212000"))

      result mustBe Seq(
        declaration,
        declaration.copy(lrn = LocalReferenceNumber("LocalReference2")),
        declaration.copy(lrn = LocalReferenceNumber("LocalReference3"))
      )
    }

    "return an empty list list if no stored declarations match the input EORI" in {
      val preTestSetup1 = await(repository.upsert(declaration))
      val preTestSetup2 = await(repository.upsert(declaration.copy(eori = "GB205672212001")))
      preTestSetup1 mustBe Right(())
      preTestSetup2 mustBe Right(())

      val result = await(repository.get("GB205672212002"))

      result mustBe List.empty
    }
  }

  ".get (by EORI & LRN)" should {
    "return a declaration that matches the input EORI & LRN" in {
      val preTestSetup1 = await(repository.upsert(declaration))
      val preTestSetup2 = await(repository.upsert(declaration.copy(eori = "GB205672212001")))
      preTestSetup1 mustBe Right(())
      preTestSetup2 mustBe Right(())

      val result = await(repository.get("GB205672212000", LocalReferenceNumber("LocalReference1")))

      result mustBe Some(declaration)
    }

    "return an error if a matching declaration is not found" in {
      val preTestSetup1 = await(repository.upsert(declaration))
      val preTestSetup2 = await(repository.upsert(declaration.copy(eori = "GB205672212001")))
      preTestSetup1 mustBe Right(())
      preTestSetup2 mustBe Right(())

      val result = await(repository.get("GB205672212000", LocalReferenceNumber("LocalReference2")))

      result mustBe None
    }
  }

}
