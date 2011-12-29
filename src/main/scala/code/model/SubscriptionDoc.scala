/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb._
import mongodb.{JsonObjectMeta, JsonObject}
import mongodb.record._
import field._
import common._
import record.field._
import util._

import notify.client._

case class Email(to: List[String])

case class NotifyOptions(email: Box[Email]) extends JsonObject[NotifyOptions] {
	def meta = NotifyOptions
}

object NotifyOptions extends JsonObjectMeta[NotifyOptions]


class NotifySubscriptionDoc extends MongoRecord[NotifySubscriptionDoc] 
								with ObjectIdPk[NotifySubscriptionDoc] {
  
  object who extends ObjectIdRefField(this, UserDoc)

  object onWhat extends EnumField(this, NotifyEvents)

  object output extends JsonObjectField[NotifySubscriptionDoc, NotifyOptions](this, NotifyOptions) {
  	def defaultValue = NotifyOptions(Empty)
  }

  def meta = NotifySubscriptionDoc
}

object NotifySubscriptionDoc extends NotifySubscriptionDoc with MongoMetaRecord[NotifySubscriptionDoc] {
  override def collectionName: String = "subscriptions_notify"
}