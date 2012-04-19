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

import notification._
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

  def events: BsonRecordListField[_, Event]
}

class Event extends BsonRecord[Event] {
  def meta = Event

  object name extends EnumNameField(this, NotifyEvents)
}

object Event  extends Event with BsonMetaRecord[Event] {
  val options = NotifyEvents.values.map(v => v -> v.toString).toSeq
} 

class Web extends BsonRecord[Web] with NotificationService {
  def meta = Web

  object activated extends BooleanField(this, false)

  object events extends BsonRecordListField[Web, Event](this, Event)
}

object Web  extends Web with BsonMetaRecord[Web] 

class Email extends BsonRecord[Email] with NotificationService {
  def meta = Email
  
  object to extends MongoListField[Email, String](this)

  object activated extends BooleanField(this, false)

  object events extends BsonRecordListField[Email, Event](this, Event)

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

  object web extends BsonRecordField(this, Web)

  object email extends BsonRecordField(this, Email)
}

object NotifyOptions extends NotifyOptions with BsonMetaRecord[NotifyOptions]


class NotifySubscriptionDoc extends MongoRecord[NotifySubscriptionDoc] 
								with ObjectIdPk[NotifySubscriptionDoc] {
  
  object who extends ObjectIdRefField(this, UserDoc)

  object repo extends ObjectIdRefField(this, RepositoryDoc)

  object output extends BsonRecordField(this, NotifyOptions) 

  def meta = NotifySubscriptionDoc
}

object NotifySubscriptionDoc extends NotifySubscriptionDoc with MongoMetaRecord[NotifySubscriptionDoc] {
  override def collectionName: String = "subscriptions_notify"
}