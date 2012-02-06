package code.model

import org.apache.commons.codec.digest.DigestUtils
import net.liftweb.record.field.{BooleanField, StringField}
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.lib.RepositoryCache.FileKey
import java.io.File
import org.eclipse.jgit.util.FS
import net.liftweb.http.S
import net.liftweb.common.{Full, Box}
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdRefField, ObjectIdPk}

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 02.10.11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */

class CollaboratorDoc  private() extends MongoRecord[CollaboratorDoc] with ObjectIdPk[CollaboratorDoc]  {
  object userId extends ObjectIdRefField(this, UserDoc)
  object repoId extends ObjectIdRefField(this, RepositoryDoc)

  def meta = CollaboratorDoc

}

object CollaboratorDoc extends CollaboratorDoc with MongoMetaRecord[CollaboratorDoc] {
  override def collectionName: String = "collaborators"


}