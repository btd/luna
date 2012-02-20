/*
   Copyright 2012 Denis Bardadym

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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