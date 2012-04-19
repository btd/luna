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

import net.liftweb._
import mongodb.record._
import field._
import common._
import record.field._
import util._

import Helpers._

import org.eclipse.jgit.lib.{Ref, PersonIdent}
import org.bson.types.ObjectId
import code.model._

object NotifyEvents extends Enumeration {
   val Push = Value("Push")
   val PullRequestOpen = Value("Open Pull Request")
   val PullRequestClose = Value("Close Pull Request")
}

/*
   This event for git-recieve-pack command.
   where - this is repository where this command appear
   who - optional user (because we can push through specific ssh key)
   pusher - this will be who make a push from JGit identification
   what - this is func that get me a seq of commits (i do not what ask sender convert for me a RevWalk to Seq)
*/
case class PushEvent(repo: ObjectId, pusher: Option[ObjectId], what: Map[String, Ref])

trait EventDoc {

  def when: DateField[_] 

  def who: ObjectIdRefField[_, _]

  def onWhat: EnumNameField[_, _]
}

trait EventBaseDoc[MyType <: EventBaseDoc[MyType]] extends MongoRecord[MyType] with ObjectIdPk[MyType] with EventDoc {
   self: MyType =>

   object when extends DateField(this) {
      override def defaultValue = new java.util.Date(millis)
   }

   

   object who extends ObjectIdRefField(this, UserDoc)
}

trait EventBaseMeta[MyType <: EventBaseDoc[MyType]] extends EventBaseDoc[MyType] with MongoMetaRecord[MyType] {
   self: MyType =>

   override def collectionName: String = "activity_events"
}


//pull request events
trait PullRequestEventDoc[MyType <: PullRequestEventDoc[MyType]] extends EventBaseDoc[MyType] {
   self: MyType =>

   object pr extends ObjectIdRefField(this.asInstanceOf[MyType], PullRequestDoc)
}

class PullRequestOpenEventDoc extends PullRequestEventDoc[PullRequestOpenEventDoc] {
   object onWhat extends EnumNameField(this, NotifyEvents) {
      override def defaultValue = NotifyEvents.PullRequestOpen
   }
   def meta = PullRequestOpenEventDoc
}
object PullRequestOpenEventDoc extends PullRequestOpenEventDoc with EventBaseMeta[PullRequestOpenEventDoc]

class PullRequestClosedEventDoc extends PullRequestEventDoc[PullRequestClosedEventDoc] {
   object onWhat extends EnumNameField(this, NotifyEvents) {
      override def defaultValue = NotifyEvents.PullRequestClose
   }
   def meta = PullRequestClosedEventDoc
}
object PullRequestClosedEventDoc extends PullRequestClosedEventDoc with EventBaseMeta[PullRequestClosedEventDoc]


//description of documents for push event

class IdentDoc extends BsonRecord[IdentDoc] {
   object email extends StringField(this, 100)

   object name extends StringField(this, 100)

   def meta = IdentDoc
}
object IdentDoc extends IdentDoc with BsonMetaRecord[IdentDoc]

class CommitDoc extends BsonRecord[CommitDoc] {
   object ident extends BsonRecordField(this, IdentDoc)

   object when extends DateField(this)

   object msg extends StringField(this, 2000)

   def meta = CommitDoc
}
object CommitDoc extends CommitDoc with BsonMetaRecord[CommitDoc]

class ChangedBranchDoc extends BsonRecord[ChangedBranchDoc] {
   object commits extends BsonRecordListField(this, CommitDoc)

   object name extends StringField(this, 20)

   def meta = ChangedBranchDoc
}

object ChangedBranchDoc extends ChangedBranchDoc with BsonMetaRecord[ChangedBranchDoc]

class PushEventDoc extends EventBaseDoc[PushEventDoc] {
   object onWhat extends EnumNameField(this, NotifyEvents) {
      override def defaultValue = NotifyEvents.Push
   }

   object repo extends ObjectIdRefField(this, RepositoryDoc)  

   object added extends MongoListField[PushEventDoc, String](this)
   object deleted extends MongoListField[PushEventDoc, String](this)
   object changed extends BsonRecordListField(this, ChangedBranchDoc)

   def meta = PushEventDoc
}

object PushEventDoc extends PushEventDoc with EventBaseMeta[PushEventDoc]

