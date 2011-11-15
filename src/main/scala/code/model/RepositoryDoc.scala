/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb._
import mongodb.record.{MongoMetaRecord, MongoRecord}
import record.field._
import util._
import org.eclipse.jgit.lib.RepositoryCache.FileKey
import org.eclipse.jgit.util.FS

import main.Main
import net.liftweb.http.S
import org.apache.commons.codec.digest.DigestUtils
import net.liftweb.mongodb.record.field.{ObjectIdRefField, ObjectIdPk}
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter

import org.eclipse.jgit.lib.{Constants, ObjectId, FileMode, RepositoryCache}
import net.liftweb.common._
import net.liftweb.util.Helpers._
import org.eclipse.jgit.api.Git
import collection.immutable.Nil
import org.eclipse.jgit.diff.DiffFormatter
import java.io.{ByteArrayOutputStream, File}
import collection.mutable.{ListBuffer, ArrayBuffer}
import org.eclipse.jgit.transport.{URIish, UploadPack, ReceivePack}


import com.foursquare.rogue.Rogue._


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

  //имя папки репозитория not null unique primary key хеш наверно SHA-1
  object fsName extends StringField(this, 50, DigestUtils.sha(id.get.toString).toString)

  //имя репозитория для пользователя not null
  object name extends StringField(this, 50) {
    private def unique_?(msg: String)(value:String): List[FieldError] = {
      if ((RepositoryDoc where (_.ownerId eqs ownerId.get) and (_.name eqs value) get) isDefined) 
        List(FieldError(this, msg)) 
      else 
        Nil 
    }

    override def validations = valMinLen(1, "Name cannot be empty") _ :: 
                                valRegex("""[a-zA-Z0-9\.\-]+""".r.pattern, "Name can contains only US-ASCII letters, digits, .(point), -(minus)") _ :: 
                                valMaxLen(maxLength, "Name cannot be more than "+maxLength+" symbols") _ ::
                                unique_?("Repository with such name already exists") _ :: super.validations
  }

  //открытый или закрытый репозиторий not null default true
  object open_? extends BooleanField(this, true)

  //id того репозитория откуда был склонирован
  object forkOf extends ObjectIdRefField(this, RepositoryDoc) {
    override def optional_? = true
  }

  // владельц репозитория not null
  object ownerId extends ObjectIdRefField(this, UserDoc)


  def owner = ownerId.obj.get


  def collaborators = CollaboratorDoc.findAll("repoId", id.get).flatMap(c => c.userId.obj)

  def keys = SshKeyDoc.findAll("ownerRepoId", id.is)

  def pullRequests: List[PullRequestDoc] = PullRequestDoc where (_.destRepoId eqs id.get) fetch


  object git {
    private lazy val fs_repo = fs_exists_? match {
      case true => RepositoryCache.open(loc)
      case false => {
        val repo = RepositoryCache.open(loc, false)
        repo.create(true /* bare */)
        repo
      }
    }

    def currentBranch = fs_repo.getBranch

    def ls_tree(path: List[String], commit: String) = {

      val reader = fs_repo.newObjectReader
      val rev = new RevWalk(reader)

      val c = rev.parseCommit(fs_repo.resolve(commit))
      var walk = new TreeWalk(reader)
      walk.addTree(c.getTree)

      val level = 0

      if (!path.isEmpty) {
        walk = subTree(walk, Nil, path)
      }

      def preparePath(rawPath: Array[Byte]) = {
        val str = new String(rawPath, "UTF-8")
        logger.debug("Path: " + str)
        str.substring(str.lastIndexOf("/") + 1)
      }

      val list = new ArrayBuffer[SourceElement](50)
      while (walk.next) {
        list +=
          (if (walk.getFileMode(level) == FileMode.TREE) Tree(preparePath(walk.getRawPath), walk.getObjectId(level))
          else Blob(preparePath(walk.getRawPath), walk.getObjectId(level)))
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
          logger.debug("Now at " + tw.getFileMode(0).getObjectType)
          if (tw.getFileMode(0).getObjectType == Constants.OBJ_TREE) tw.enterSubtree
          subTree(tw, prefix ::: List[String](subDir), other)
        }
        case Nil => tw
      }
    }

    def ls_cat(path: List[String], commit: String) = {
      //logger.debug("Try to load source " + path)
      val reader = fs_repo.newObjectReader
      val rev = new RevWalk(reader)

      val c = rev.parseCommit(fs_repo.resolve(commit))
      var walk = new TreeWalk(reader)
      walk.addTree(c.getTree)

      val level = 0

      walk = subTree(walk, Nil, path)

      var result = ""

      logger.debug("tw -> " + walk.getFileMode(level).getObjectType)

      if (walk.getFileMode(level).getObjectType == Constants.OBJ_BLOB) {
        logger.debug("Source founded. Try to load")
        val blobLoader = reader.open(walk.getObjectId(level), Constants.OBJ_BLOB)
        result = scala.io.Source.fromInputStream(blobLoader.openStream).mkString

      }

      rev.release
      walk.release
      reader.release

      result
    }

    def branches =
      scala.collection.JavaConversions.asScalaBuffer((new Git(fs_repo)).branchList.call).map(ref => ref.getName.substring(ref.getName.lastIndexOf("/") + 1))


    private def fs_exists_? = FileKey.resolve(new File(fsPath), FS.DETECTED) != null

    def inited_? = {
      //TODO сделать получше
      val rev = new RevWalk(fs_repo)
      val res = tryo {
        rev.parseCommit(fs_repo.resolve(currentBranch))
      } or {
        Empty
      }
      rev.release
      res != Empty
    }

    private lazy val loc = FileKey.lenient(new File(fsPath), FS.DETECTED)

    lazy val fsPath = Main.repoDir + fsName

    def upload_pack = new UploadPack(fs_repo)

    def receive_pack = {
      val pack = new ReceivePack(fs_repo)
      pack.setAllowCreates(true)
      pack.setAllowDeletes(true)
      pack.setAllowNonFastForwards(true)
      pack.setCheckReceivedObjects(true)

      pack
    }

    def fs_repo_! = fs_repo

    def log(commit: String) = {
      scala.collection.JavaConversions.asScalaIterator((new Git(fs_repo)).log.add(fs_repo.resolve(commit)).call.iterator)
    }

    def log(commit: String, path: String) = {
      scala.collection.JavaConversions.asScalaIterator((new Git(fs_repo)).log.add(fs_repo.resolve(commit)).addPath(path).call.iterator)
    }

    def diff(commit1: String, commit2: String) = {
      import org.eclipse.jgit.diff.DiffEntry.ChangeType._

      val diffList = new ListBuffer[String]
      val statusList = tryo {
        val baos = new ByteArrayOutputStream
        val formatter = new DiffFormatter(baos)
        formatter.setRepository(fs_repo)
        formatter.setDetectRenames(true)


        val entries = scala.collection.JavaConversions.asScalaBuffer(formatter.scan(fs_repo.resolve(commit1), fs_repo.resolve(commit2)))

        val sl = entries.map {
          ent =>
            formatter.format(ent)
            formatter.flush

            diffList += baos.toString("UTF-8")

            baos.reset

            ent.getChangeType match {
              case ADD => Added(ent.getNewPath)
              case DELETE => Deleted(ent.getOldPath)
              case MODIFY => Modified(ent.getNewPath)
              case COPY => Copied(ent.getOldPath, ent.getNewPath)
              case RENAME => Renamed(ent.getOldPath, ent.getNewPath)
            }
        }


        formatter.release

        sl.toList
      } openOr {
        Nil
      }

      (statusList -> diffList.toList)
    }

    def diff(commit: String): Pair[List[Change], List[String]] = diff(commit + "^1", commit)

    def clone(user: UserDoc): RepositoryDoc = {
      val uri = new URIish(fsPath)
      val clonnedDoc = RepositoryDoc.createRecord.ownerId(user.id.get).name(chooseCloneName(user)).forkOf(id.get)
      Git.cloneRepository.setURI(uri.toString)
        .setDirectory(new File(clonnedDoc.git.fsPath))
        .setBare(true)
        .setCloneAllBranches(true)
        .call
      clonnedDoc.save
    }

    private def chooseCloneName(user: UserDoc): String = {
      user.repos.filter(_.name.get.startsWith(name.get)).map(_.name.get) match {
        case Nil => name.get
        case l => name.get + "-" + (l.map(n => tryo {
          asInt(n.split("-")(1))
        } openOr {
          Empty
        }).map((x: Box[Int]) => (x match {
          case Full(i: Int) => i
          case _ => 0
        })).max + 1)
      }
    }

  }


  lazy val pullRequestUrl = homePageUrl + "/pull-requests"

  lazy val homePageUrl = "/" + owner.login.get + "/" + name.get

  lazy val sourceTreeUrl = homePageUrl + "/tree/" + git.currentBranch

  lazy val commitsUrl = homePageUrl + "/commits/" + git.currentBranch

  def commitsUrl(commit: String) = homePageUrl + "/commits/" + commit

  def commitUrl(commit: String) = homePageUrl + "/commit/" + commit

  def sourceTreeUrl(commit: String) = homePageUrl + "/tree/" + commit

  def sourceBlobUrl(commit: String) = homePageUrl + "/blob/" + commit

  lazy val publicGitUrl = "git://" + S.hostName + "/" + owner.login.get + "/" + name.get

  lazy val privateSshUrl = owner.login.get + "@" + S.hostName + ":" + name.get

  def privateSshUrl(user: UserDoc): String = owner_?(Full(user)) match {
    case true => privateSshUrl
    case false => user.login.is + "@" + S.hostName + ":" + owner.login.get + "/" + name.get
  }

  def canPush_?(user: Box[UserDoc]) = {
    //logger.debug(user)
    user match {
      case Full(u) if u.login.get == owner.login.get => true //владелец
      case Full(u) => !collaborators.filter(_.login.get == u.login.get).isEmpty //коллаборатор
      case _ => false
    }
  }

  def cloneUrls(user: Box[UserDoc]) = canPush_?(user) match {
    case true => (publicGitUrl, "Git") :: (privateSshUrl(user.get), "Ssh") :: Nil
    case _ => (publicGitUrl, "Git") :: Nil
  }

  def owner_?(user: Box[UserDoc]) = user match {
    case Full(u) if (u.login.get == owner.login.get) => true
    case _ => false
  }

  def deleteDependend = {
    CollaboratorDoc where (_.repoId eqs id.get) bulkDelete_!!

      PullRequestDoc where (_.destRepoId eqs id.get) bulkDelete_!!

      PullRequestDoc where (_.srcRepoId eqs id.get) bulkDelete_!!

      SshKeyDoc where (_.ownerRepoId eqs id.get) bulkDelete_!!

      RepositoryDoc where (_.forkOf eqs id.get) modify (_.forkOf setTo null) updateMulti
  }

  def meta = RepositoryDoc

}

object RepositoryDoc extends RepositoryDoc with MongoMetaRecord[RepositoryDoc] {
  override def collectionName: String = "repositories"

  def allClonesExceptOwner(repo: RepositoryDoc) =
    RepositoryDoc where (_.forkOf eqs repo.forkOf.get) and (_.ownerId neqs repo.ownerId.get) fetch
}


abstract class Change {}

case class Added(path: String) extends Change {}

case class Deleted(path: String) extends Change {}

case class Modified(path: String) extends Change {}

case class Copied(oldPath: String, newPath: String) extends Change {}

case class Renamed(oldPath: String, newPath: String) extends Change {}