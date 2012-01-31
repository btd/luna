package code.model

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record._
import net.liftweb.record.field._
import net.liftweb.record._
import java.util.Date

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 22.10.11
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */

class PullRequestDoc private() extends MongoRecord[PullRequestDoc] with ObjectIdPk[PullRequestDoc] {

  object srcRepoId extends ObjectIdRefField(this, RepositoryDoc)

  object destRepoId extends ObjectIdRefField(this, RepositoryDoc)

  object srcRef extends StringField(this, 30)

  object destRef extends StringField(this, 30)

  object creatorId extends ObjectIdRefField(this, UserDoc)

  object creationDate extends DateField(this)

  object accepted_? extends BooleanField(this, false)

  object description extends StringField(this, 1000)

  lazy val homePageUrl = destRepoId.obj.get.homePageUrl + "/pull-request/" + id.get

  def srcRepo = srcRepoId.obj.get

  def destRepo = destRepoId.obj.get

  def meta = PullRequestDoc

}

object PullRequestDoc extends PullRequestDoc with MongoMetaRecord[PullRequestDoc] {
  override def collectionName: String = "pull_requests"
}

