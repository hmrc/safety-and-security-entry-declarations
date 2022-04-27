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

package generators

import models.MessageType.{Amendment, Submission}
import models.{CorrelationId, LocalReferenceNumber, MessageType, MovementReferenceNumber, Outcome, RejectionReason}
import models.Outcome.{Accepted, Rejected}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.Instant

trait ModelGenerators {

  implicit lazy val arbitraryLocalReferenceNumber: Arbitrary[LocalReferenceNumber] =
    Arbitrary {
      for {
        numChars <- Gen.choose(1, 22)
        chars <- Gen.listOfN(numChars, Gen.alphaNumChar)
      } yield LocalReferenceNumber(chars.mkString)
    }

  implicit lazy val arbitraryCorrelationId: Arbitrary[CorrelationId] =
    Arbitrary {
      Gen.numStr.map(CorrelationId.apply)
    }

  implicit lazy val arbitraryMessageType: Arbitrary[MessageType] =
    Arbitrary{
      Gen.oneOf(Gen.const(Submission), Gen.const(Amendment))
    }

  implicit lazy val arbitraryInstant: Arbitrary[Instant] =
    Arbitrary {
      Gen.choose(Instant.MIN, Instant.MAX)
    }

  implicit lazy val arbitraryMrn: Arbitrary[MovementReferenceNumber] =
    Arbitrary {
      for {
        yearDigits <- Gen.choose(22, 99).map(_.toString)
        nextDigits <- Gen.listOfN(2, Gen.numChar).map(_.mkString)
        finalDigits <- Gen.listOfN(12, Gen.numChar).map(_.mkString)
      } yield MovementReferenceNumber(s"${yearDigits}GB${nextDigits}I${finalDigits}")
    }

  implicit lazy val arbitraryAcceptedOutcome: Arbitrary[Accepted] =
    Arbitrary {
      for {
        correlationId <- arbitrary[CorrelationId]
        messageType <- arbitrary[MessageType]
        instant <- arbitrary[Instant]
        mrn <- arbitrary[MovementReferenceNumber]
      } yield Accepted(correlationId, messageType, instant, mrn)
    }

  implicit lazy val arbitraryRejectionReason: Arbitrary[RejectionReason] = {
    Arbitrary {
      for {
        code <- Gen.option(Gen.choose(1000, 9000))
        desc <- arbitrary[String]
      } yield RejectionReason(code, desc)
    }
  }

  implicit lazy val arbitraryRejectedOutcome: Arbitrary[Rejected] =
    Arbitrary {
      for {
        correlationId <- arbitrary[CorrelationId]
        messageType <- arbitrary[MessageType]
        instant <- arbitrary[Instant]
        reason <- arbitrary[RejectionReason]
      } yield Rejected(correlationId, messageType, instant, reason)
    }

  implicit lazy val arbitraryOutcome: Arbitrary[Outcome] =
    Arbitrary{
      Gen.oneOf(arbitrary[Accepted], arbitrary[Rejected])
    }
}
