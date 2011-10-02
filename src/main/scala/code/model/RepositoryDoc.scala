/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.{BooleanField, StringField}
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.lib.RepositoryCache.FileKey
import java.io.File
import org.eclipse.jgit.util.FS
import net.liftweb.json.JsonDSL._
import main.Main
import net.liftweb.http.S
import net.liftweb.common.{Box, Empty, Full}
import org.apache.commons.codec.digest.DigestUtils
import net.liftweb.mongodb.record.field.{ObjectIdRefField, ObjectIdPk}

/**
 * User: denis.bardadym
 * Date: 9/30/11
 * Time: 3:05 PM
 */

class RepositoryDoc  private() extends MongoRecord[RepositoryDoc] with ObjectIdPk[RepositoryDoc]  {
  object fsName extends StringField(this, 50, DigestUtils.sha(id.get.toString).toString) //имя папки репозитория not null unique primary key хеш наверно SHA-1
  object name extends StringField(this, 50) //имя репозитория для пользователя not null
  object open_? extends BooleanField(this, true)//открытый или закрытый репозиторий not null default true
                   //val clonnedFrom: String, //id того репозитория откуда был склонирован
  object ownerId extends ObjectIdRefField(this, UserDoc) // владельц репозитория not null

  lazy val collaborators = CollaboratorDoc.findAll("repoId", id.get).flatMap(c => c.userId.obj)

  lazy val keys = SshKeyDoc.findAll("ownerRepoId", id.is)

  lazy val git =
    exists_? match {
      case true =>  RepositoryCache.open(loc)
      case false => {
        val repo = RepositoryCache.open(loc, false)
        repo.create(true /* bare */)
        repo
      }
    }


  def exists_? = FileKey.resolve(new File(fsPath), FS.DETECTED) != null

  private lazy val loc = FileKey.lenient(new File(fsPath), FS.DETECTED)

  lazy val fsPath = Main.repoDir + fsName

  lazy val publicGitUrl = "git://" + S.hostName + "/" + ownerId.obj.open_!.login.get + "/" + name.get

  lazy val privateSshUrl = ownerId.obj.open_!.login.get + "@" + S.hostName + ":" + name.get

  def privateSshUrl(user: UserDoc) = user.login.is + "@" + S.hostName + ":" + ownerId.obj.open_!.login.get + "/" + name.get

  def canPush_?(user: Box[UserDoc]) = {
    user match {
      case Full(u) if u.login.get == ownerId.obj.open_!.login.get => true  //владелец
      case _ => false
    }
  }

  def meta = RepositoryDoc

}

object RepositoryDoc extends RepositoryDoc with MongoMetaRecord[RepositoryDoc] {
  override def collectionName: String = "repositories"
}