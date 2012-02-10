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
import java.io._
import collection.mutable.{ListBuffer, ArrayBuffer}
import org.eclipse.jgit.transport.{URIish, UploadPack, ReceivePack}


import com.foursquare.rogue.Rogue._

import org.lunatool.linguist._
import Helper._

/**
 * User: denis.bardadym
 * Date: 9/30/11
 * Time: 3:05 PM
 */

abstract class SourceElement {
  def path: String

  def basename = path.substring(path.lastIndexOf("/") + 1)
}

abstract case class Blob(path: String, size: Long) extends SourceElement with BlobHelper with FileBlob {
  def basePath = None
 
  def name = path
}

case class Tree(path: String) extends SourceElement

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

  def keys = SshKeyRepoDoc.findAll("ownerId", id.is)

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
      logger.debug("ls_tree" + path)
      val reader = fs_repo.newObjectReader
      val rev = new RevWalk(reader)

      val c = rev.parseCommit(fs_repo.resolve(commit))
      var walk = new TreeWalk(reader)
      walk.addTree(c.getTree)

      val level = 0

      if (!path.isEmpty) {
        walk = subTree(walk, Nil, path)
      }

      val list = new ArrayBuffer[SourceElement](50)
      while (walk.next) {
        val fullPath = guessString(Some(walk.getRawPath)) getOrElse ""

        list +=
          (if (walk.getFileMode(level) == FileMode.TREE) Tree(fullPath)
          else new Blob(fullPath, reader.getObjectSize(walk.getObjectId(level), Constants.OBJ_BLOB)) {
            lazy val data = ls_cat(fullPath.split("/").toList, commit)
          })
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

    def withSourceElementStream[T](path: List[String], commit: String)(f: (InputStream) => T): Option[T]  = {
      logger.debug("ls_cat" + path)
      val reader = fs_repo.newObjectReader
      val rev = new RevWalk(reader)

      val c = rev.parseCommit(fs_repo.resolve(commit))
      var walk = new TreeWalk(reader)
      walk.addTree(c.getTree)

      val level = 0

      walk = subTree(walk, Nil, path)

      var result: Option[T] = None

      logger.debug("tw -> " + walk.getFileMode(level).getObjectType)

      if (walk.getFileMode(level).getObjectType == Constants.OBJ_BLOB) {
        logger.debug("Source founded. Try to load")
        val blobLoader = reader.open(walk.getObjectId(level), Constants.OBJ_BLOB)

        val in = blobLoader.openStream
        result = Some(f(in)) 

      }

      rev.release
      walk.release
      reader.release

      result
    }

    def ls_cat(path: List[String], commit: String): Option[String] = 
      withSourceElementStream(path, commit){in => 
        val r = inputStreamToByteArray(in) 
        in.close
        r
      }.flatMap(guessString)
    

    def branches =
      scala.collection.JavaConversions.asScalaBuffer((new Git(fs_repo)).branchList.call).map(ref => ref.getName.substring(ref.getName.lastIndexOf("/") + 1))

    def refsHeads = scala.collection.JavaConversions.asScalaBuffer((new Git(fs_repo)).branchList.call)

    def setCurrentBranch(newBranch: org.eclipse.jgit.lib.Ref) = {
      import org.eclipse.jgit.lib._

      fs_repo.updateRef(Constants.HEAD, false).link(newBranch.getName)
    }


    private def fs_exists_? = FileKey.resolve(new File(fsPath), FS.DETECTED) != null

    def inited_? = {
      //TODO сделать получше getRef(Constants.HEAD).getObjectId == null ?
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

    lazy val fsPath = Props.get(main.Constants.REPOSITORIES_DIR, "./repo/") + fsName.get

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
      logger.debug("Try to get log for " + path)
      scala.collection.JavaConversions.asScalaIterator((new Git(fs_repo)).log.add(fs_repo.resolve(commit)).addPath(path).call.iterator)
    }

    def log(from: ObjectId, to: ObjectId) = {
   
      scala.collection.JavaConversions.asScalaIterator(
        (new Git(fs_repo))
          .log.addRange(from, to).call.iterator)
    }

    def diff(commit1: String, commit2: String) = {
      import org.eclipse.jgit.diff.DiffEntry.ChangeType._

      val diffList = new ListBuffer[(Change, String)]
      try {
        val baos = new ByteArrayOutputStream
        val formatter = new DiffFormatter(baos)
        formatter.setRepository(fs_repo)
        formatter.setDetectRenames(true)

        val entries = scala.collection.JavaConversions.asScalaBuffer(formatter.scan(fs_repo.resolve(commit1), fs_repo.resolve(commit2)))

        for(entry <- entries) {
            formatter.format(entry)
            formatter.flush

            diffList += (((entry.getChangeType match {
                          case ADD => Added(entry.getNewPath)
                          case DELETE => Deleted(entry.getOldPath)
                          case MODIFY => Modified(entry.getNewPath)
                          case COPY => Copied(entry.getOldPath, entry.getNewPath)
                          case RENAME => Renamed(entry.getOldPath, entry.getNewPath)
                        }),baos.toString("UTF-8"))) 

            baos.reset            
        }

        formatter.release
      } catch {
        case _ =>
      }
      diffList.toList
    }

    def diff(commit: String): List[(Change, String)] = diff(commit + "^1", commit)

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

    def resolve(ref: String) = ObjectId.toString(fs_repo.resolve(ref))

  }


  lazy val pullRequestsUrl = homePageUrl + "/pull-requests"

  lazy val homePageUrl = "/" + owner.login.get + "/" + name.get

  lazy val sourceTreeUrl = homePageUrl + "/tree/" + git.currentBranch

  lazy val commitsUrl = homePageUrl + "/commits/" + git.currentBranch

  def commitsUrl(commit: String) = homePageUrl + "/commits/" + commit

  def commitUrl(commit: String) = homePageUrl + "/commit/" + commit

  def sourceTreeUrl(commit: String) = homePageUrl + "/tree/" + commit

  def sourceBlobUrl(commit: String) = homePageUrl + "/blob/" + commit

  lazy val publicGitUrl = "git://" + S.hostName + "/" + owner.login.get + "/" + name.get

  lazy val privateSshUrl = owner.login.get + "@" + S.hostName + ":" + name.get

  def privateSshUrl(user: Box[UserDoc]): String = owner_?(user) match {
    case true => privateSshUrl
    case false => user.get.login.is + "@" + S.hostName + ":" + owner.login.get + "/" + name.get
  }

  def canPush_?(user: Box[UserDoc]) = {
    //logger.debug(user)
    user match {
      case Full(u) if u.login.get == owner.login.get => true //владелец
      case Full(u) => !collaborators.filter(_.login.get == u.login.get).isEmpty //коллаборатор
      case _ => false
    }
  }

  def cloneUrlsForCurrentUser = canPush_?(UserDoc.currentUser) match {
    case true => (publicGitUrl, "Git") :: (privateSshUrl(UserDoc.currentUser), "Ssh") :: Nil
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

      SshKeyRepoDoc where (_.ownerId eqs id.get) bulkDelete_!!

      RepositoryDoc where (_.forkOf eqs id.get) modify (_.forkOf setTo null) updateMulti
  }

  def meta = RepositoryDoc

}

object RepositoryDoc extends RepositoryDoc with MongoMetaRecord[RepositoryDoc] {
  override def collectionName: String = "repositories"

  def allClonesExceptOwner(repo: RepositoryDoc) =
    RepositoryDoc where (_.forkOf eqs repo.forkOf.get) and (_.ownerId neqs repo.ownerId.get) fetch

  def byUserLoginAndRepoName(login: String, repoName: String): Option[RepositoryDoc] = {
    (UserDoc where (_.login eqs login) get) flatMap (u => 
      (RepositoryDoc where (_.ownerId eqs u.id.get) and (_.name eqs repoName) get))
  }
}


trait Change 

case class Added(path: String) extends Change

case class Deleted(path: String) extends Change

case class Modified(path: String) extends Change

case class Copied(oldPath: String, newPath: String) extends Change

case class Renamed(oldPath: String, newPath: String) extends Change