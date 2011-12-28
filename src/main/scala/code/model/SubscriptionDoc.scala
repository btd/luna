/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb._
import mongodb.record._
import field._
import common._
import record.field._
import util._

import notify.client._



class NotifySubscriptionDoc extends MongoRecord[NotifySubscriptionDoc] 
								with ObjectIdPk[NotifySubscriptionDoc] {
  
  object who extends ObjectIdRefField(this, UserDoc)

  object onWhat extends EnumField(this, NotifyEvents)

  def meta = NotifySubscriptionDoc
}

object NotifySubscriptionDoc extends NotifySubscriptionDoc with MongoMetaRecord[NotifySubscriptionDoc] {
  override def collectionName: String = "subscriptions_notify"
}