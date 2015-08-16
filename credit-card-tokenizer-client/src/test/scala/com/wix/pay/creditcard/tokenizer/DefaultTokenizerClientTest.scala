package com.wix.pay.creditcard.tokenizer


import com.google.api.client.http.javanet.NetHttpTransport
import com.twitter.util.{Return, Throw}
import com.wix.pay.creditcard.tokenizer.model._
import com.wix.pay.creditcard.tokenizer.testkit.TokenizerDriver
import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, PublicCreditCard, YearMonth}
import com.wix.restaurants.common.protocol.api.Error
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class DefaultTokenizerClientTest extends SpecWithJUnit {
  val cardsStoreBridgePort = 10001

  val someAccessToken = "some access token"
  val someCard = CreditCard(
    number = "4111111111111111",
    expiration = YearMonth(
      year = 2020,
      month = 12))
  val someInTransitToken = CreditCardToken(
    token = "some in-transit token",
    creditCard = PublicCreditCard(someCard))
  val somePermanentCardToken = CreditCardToken(
    token = "some permanent token",
    creditCard = PublicCreditCard(someCard))

  val aTokenizeRequest = TokenizeRequest(card = someCard)
  val someAdditionalCardInfo = Some(CreditCardOptionalFields.withFields(
    csc = Some("123")
  ))
  val anInTransitRequest = InTransitRequest(
    permanentToken = somePermanentCardToken,
    additionalInfo = someAdditionalCardInfo
  )

  val cardsStoreBridge = new DefaultTokenizerClient(
    requestFactory = new NetHttpTransport().createRequestFactory(),
    endpointUrl = s"http://localhost:$cardsStoreBridgePort")

  val driver = new TokenizerDriver(port = cardsStoreBridgePort)

  val anInternalError: String => Error = message => Error(
      code = ErrorCodes.internal,
      description = message)


  step {
    driver.start()
  }


  sequential


  trait Ctx extends Scope {
    driver.reset()
  }


  "tokenizing a card" should {
    "return an in-transit card token on success" in new Ctx {
      driver.aTokenizeFor(aTokenizeRequest) returns someInTransitToken

      cardsStoreBridge.tokenize(
        card = someCard
      ) must be_===(Return(someInTransitToken))
    }

    "gracefully fail on error" in new Ctx {
      val someErrorMessage = "some error message"
      driver.aTokenizeFor(aTokenizeRequest) errors anInternalError(someErrorMessage)

      cardsStoreBridge.tokenize(
        card = someCard
      ) must be_===(Throw(TokenizerInternalException(someErrorMessage)))
    }
  }

  "converting a permanent card token" should {
    "return an in-transit card token on success" in new Ctx {
      driver.anInTransitFor(anInTransitRequest) returns someInTransitToken

      cardsStoreBridge.inTransit(
        permanentToken = somePermanentCardToken,
        additionalInfo = someAdditionalCardInfo
      ) must be_===(Return(someInTransitToken))
    }

    "gracefully fail on error" in new Ctx {
      val someErrorMessage = "some error message"

      driver.anInTransitFor(anInTransitRequest) errors anInternalError(someErrorMessage)

      cardsStoreBridge.inTransit(
        permanentToken = somePermanentCardToken,
        additionalInfo = someAdditionalCardInfo
      ) must be_===(Throw(TokenizerInternalException(someErrorMessage)))
    }
  }

  step {
    driver.stop()
  }
}
