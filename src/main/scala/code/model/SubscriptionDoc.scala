/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model


import notification.client._

import net.liftweb._
import json.{Formats, DefaultFormats, ShortTypeHints, Serialization}
import json.JsonAST._
import mongodb.{JsonObjectMeta, JsonObject}
import mongodb.record._
import field._
import common._
import record.field._
import util._

trait NotificationService {
  def activated: BooleanField[_]
}

class Email extends BsonRecord[Email] with NotificationService {
  def meta = Email
  
  object to extends MongoListField[Email, String](this)

  object activated extends BooleanField(this, false)

  override def asJValue = {
    if(activated.get) {
        JObject(JField(to.name, JString(to.get.mkString(";"))) :: Nil)
    } else {
        JObject(Nil)
    }
  }
}

object Email extends Email with BsonMetaRecord[Email]

class NotifyOptions extends BsonRecord[NotifyOptions] {
	def meta = NotifyOptions

  object email extends BsonRecordField(this, Email)
}

object NotifyOptions extends NotifyOptions with BsonMetaRecord[NotifyOptions]


class NotifySubscriptionDoc extends MongoRecord[NotifySubscriptionDoc] 
								with ObjectIdPk[NotifySubscriptionDoc] {
  
  object who extends ObjectIdRefField(this, UserDoc)

  object repo extends ObjectIdRefField(this, RepositoryDoc)

  object onWhat extends EnumNameField(this, NotifyEvents)

  object output extends BsonRecordField(this, NotifyOptions) 

  def meta = NotifySubscriptionDoc
}

object NotifySubscriptionDoc extends NotifySubscriptionDoc with MongoMetaRecord[NotifySubscriptionDoc] {
  override def collectionName: String = "subscriptions_notify"
}