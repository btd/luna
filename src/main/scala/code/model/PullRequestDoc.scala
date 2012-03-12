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

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record._
import net.liftweb.record.field._
import net.liftweb.record._
import java.util.Date




class PullRequestDoc private() extends MongoRecord[PullRequestDoc] with ObjectIdPk[PullRequestDoc] {

  object srcRepoId extends ObjectIdRefField(this, RepositoryDoc)

  object destRepoId extends ObjectIdRefField(this, RepositoryDoc)

  object srcRef extends StringField(this, 30)

  object destRef extends StringField(this, 30)

  object creatorId extends ObjectIdRefField(this, UserDoc)

  object creationDate extends DateField(this)

  object accepted_? extends BooleanField(this, false)

  object description extends StringField(this, 1000)

  //lazy val homePageUrl = destRepoId.obj.get.homePageUrl + "/pull-request/" + id.get

  def srcRepo = srcRepoId.obj.get

  def destRepo = destRepoId.obj.get

  def meta = PullRequestDoc

}

object PullRequestDoc extends PullRequestDoc with MongoMetaRecord[PullRequestDoc] {
  override def collectionName: String = "pull_requests"
}

