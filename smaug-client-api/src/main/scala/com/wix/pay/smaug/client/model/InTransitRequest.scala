/*      __ __ _____  __                                              *\
**     / // // /_/ |/ /          Wix                                 **
**    / // // / /|   /           (c) 2006-2017, Wix LTD.             **
**   / // // / //   |            http://www.wix.com/                 **
**   \__/|__/_//_/| |                                                **
\*                |/                                                 */
package com.wix.pay.smaug.client.model


import com.wix.pay.creditcard.CreditCardOptionalFields


/** Encapsulates the data for an In-Transit request.
  *
  * @author <a href="mailto:ohadr@wix.com">Raz, Ohad</a>
  */
case class InTransitRequest(permanentToken: CreditCardToken,
                            additionalInfo: Option[CreditCardOptionalFields] = None,
                            tenantId: String)
