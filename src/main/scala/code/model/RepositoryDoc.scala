/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.{BooleanField, StringField}
import org.eclipse.jgit.lib.RepositoryCache.FileKey
import java.io.File
import org.eclipse.jgit.util.FS
import net.liftweb.json.JsonDSL._
import main.Main
import net.liftweb.http.S
import org.apache.commons.codec.digest.DigestUtils
import net.liftweb.mongodb.record.field.{ObjectIdRefField, ObjectIdPk}
import collection.mutable.ArrayBuffer
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.{CanonicalTreeParser, TreeWalk}
import org.eclipse.jgit.treewalk.filter.{PathFilter, TreeFilter}
import java.lang.String
import org.eclipse.jgit.lib.{Constants, ObjectId, FileMode, RepositoryCache}
import net.liftweb.common.{Loggable, Box, Empty, Full}

/**
 * User: denis.bardadym
 * Date: 9/30/11
 * Time: 3:05 PM
 */

abstract class SourceElement {

}

case class Blob(path: String, id: ObjectId) extends SourceElement {}

case class Tree(path: String, id: ObjectId) extends SourceElement {

}

class RepositoryDoc private() extends MongoRecord[RepositoryDoc] with ObjectIdPk[RepositoryDoc] with Loggable {

  object fsName extends StringField(this, 50, DigestUtils.sha(id.get.toString).toString)

  //имя папки репозитория not null unique primary key хеш наверно SHA-1
  object name extends StringField(this, 50)

  //имя репозитория для пользователя not null
  object open_? extends BooleanField(this, true)

  //открытый или закрытый репозиторий not null default true
  //val clonnedFrom: String, //id того репозитория откуда был склонирован
  object ownerId extends ObjectIdRefField(this, UserDoc)

  // владельц репозитория not null

  def owner = ownerId.obj.get

  lazy val homePage = "/" + owner.login.get + "/" + name.get


  lazy val collaborators = CollaboratorDoc.findAll("repoId", id.get).flatMap(c => c.userId.obj)

  lazy val keys = SshKeyDoc.findAll("ownerRepoId", id.is)

  lazy val git =
    exists_? match {
      case true => RepositoryCache.open(loc)
      case false => {
        val repo = RepositoryCache.open(loc, false)
        repo.create(true /* bare */)
        repo
      }
    }

  //TODO может стоит подгрузить все целиков вначале?
  def ls_tree(path: List[String]) = {

    val reader = git.newObjectReader
    val rev = new RevWalk(reader)

    val c = rev.parseCommit(git.resolve("HEAD"))
    var walk = new TreeWalk(reader)
    walk.addTree(c.getTree)

    val level = 0

    if(!path.isEmpty) {
       walk = subTree(walk, Nil, path)
    }

    def preparePath(rawPath: Array[Byte]) = {
      val str = new String(rawPath, "UTF-8")
       str.substring(str.lastIndexOf("/") + 1)
    }

    val list = new ArrayBuffer[SourceElement](50)
    while (walk.next) {
      list +=
      (if (walk.getFileMode(level) == FileMode.TREE) Tree(preparePath(walk.getRawPath), walk.getObjectId(level))
      else Blob(preparePath(walk.getRawPath), walk.getObjectId(level))  )
    }

    rev.release
    walk.release
    reader.release

    list.toList
  }

  private def subTree(tw: TreeWalk, prefix: List[String], suffix: List[String]): TreeWalk = {
    logger.debug("Try to load source " + suffix + " tw -> " + tw.getFileMode(0).getObjectType)
     suffix match {
       case subDir :: other => {
         tw.setFilter(PathFilter.create((prefix ::: List[String](subDir)).mkString("/")))
         logger.debug("Filter: " + (prefix ::: List[String](subDir)).mkString("/"))
         tw.next
         logger.debug("Now at " +  tw.getFileMode(0).getObjectType)
         if (tw.getFileMode(0).getObjectType == Constants.OBJ_TREE) tw.enterSubtree
         subTree(tw, prefix ::: List[String](subDir), other)
       }
       case Nil => tw
     }
  }

  def ls_cat(path: List[String]) = {
    //logger.debug("Try to load source " + path)
     val reader = git.newObjectReader
    val rev = new RevWalk(reader)

    val c = rev.parseCommit(git.resolve("HEAD"))
    var walk = new TreeWalk(reader)
    walk.addTree(c.getTree)

    val level = 0

    walk = subTree(walk, Nil, path)

    var result = ""

    logger.debug("tw -> " + walk.getFileMode(level).getObjectType)

    if (walk.getFileMode(level).getObjectType == Constants.OBJ_BLOB) {
      logger.debug("Source founded. Try to load")
      val blobLoader = reader.open(walk.getObjectId(level), Constants.OBJ_BLOB)
      result = scala.io.Source.fromInputStream(blobLoader.openStream, "UTF-8").mkString

    }

    rev.release
    walk.release
    reader.release

    result
  }


  def exists_? = FileKey.resolve(new File(fsPath), FS.DETECTED) != null

  private lazy val loc = FileKey.lenient(new File(fsPath), FS.DETECTED)

  lazy val fsPath = Main.repoDir + fsName

  lazy val publicGitUrl = "git://" + S.hostName + "/" + ownerId.obj.open_!.login.get + "/" + name.get

  lazy val privateSshUrl = ownerId.obj.open_!.login.get + "@" + S.hostName + ":" + name.get

  def privateSshUrl(user: UserDoc) = user.login.is + "@" + S.hostName + ":" + ownerId.obj.open_!.login.get + "/" + name.get

  def canPush_?(user: Box[UserDoc]) = {
    user match {
      case Full(u) if u.login.get == owner.login.get => true //владелец
      case Full(u) => collaborators.filter(_.login.get == u.login.get).isEmpty //коллаборатор
      case _ => false
    }
  }

  def meta = RepositoryDoc

}

object RepositoryDoc extends RepositoryDoc with MongoMetaRecord[RepositoryDoc] {
  override def collectionName: String = "repositories"
}