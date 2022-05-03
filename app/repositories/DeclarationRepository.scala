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

import models.{APIError, CorrelationId, Declaration, DeclarationEvent, DeclarationNotfound, LocalReferenceNumber, UnknownErrorInsertingRecord}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationRepository @Inject() (
                                    val mongoComponent: MongoComponent
                                  )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[Declaration](
    collectionName = "declarations",
    mongoComponent = mongoComponent,
    domainFormat = Declaration.format,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("eori", "lrn"),
        IndexOptions()
          .name("eoriAndLrnIdx")
          .unique(true)
      )
    )
  ) {

  private def byEORIandLrn(eori: String, lrn: LocalReferenceNumber): Bson =
    Filters.and(
      Filters.equal("eori", eori),
      Filters.equal("lrn", lrn.value)
    )

  private def set(eori: String, lrn: LocalReferenceNumber)(f: Declaration => Declaration): Future[Either[APIError, Unit]] = {
    collection.find(byEORIandLrn(eori, lrn)).headOption flatMap {
      case Some(result) =>
        upsert(f(result))
      case _ =>
        Future(Left(DeclarationNotfound))
    }
  }

  def upsert(declaration: Declaration): Future[Either[APIError, Unit]] = {
    collection.replaceOne(
      filter = byEORIandLrn(declaration.eori, declaration.lrn),
      replacement = declaration,
      options = new ReplaceOptions().upsert(true)
    ).toFuture().map(_ => Right(()))
      .recover {
        case _ => Left(UnknownErrorInsertingRecord)
      }
  }

  def insertEvent(eori: String, lrn: LocalReferenceNumber, correlationId: CorrelationId, declarationEvent: DeclarationEvent): Future[Either[APIError, Unit]] = {
    set(eori, lrn){_.withDeclarationEvent(correlationId, declarationEvent)}
  }

  def get(eori: String, lrn: LocalReferenceNumber): Future[Option[Declaration]] =
    collection
        .find(byEORIandLrn(eori, lrn))
        .headOption
}