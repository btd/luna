package daemon

import code.model._
import java.io._
import net.liftweb._
import common._
import util._
import actors.Actor


trait Service {

	def shutdown(): Unit

	def init(): Unit
}

trait Resolver {

  self: Loggable =>

  val GIT_UPLOAD_PACK = "git-upload-pack"

  val GIT_RECEIVE_PACK = "git-receive-pack"

  type packProcessor = (InputStream, OutputStream, OutputStream) => Unit

  def packProcessing(r: Box[RepositoryDoc], 
                     processor: (RepositoryDoc) => packProcessor,
                     accessible: (RepositoryDoc) => Boolean = (r) => {true}):
      Box[packProcessor] = {
    for{repo <- r; if(accessible(repo))} yield {
       processor(repo)
    }
  }  

  def inParallel(in: InputStream, out: OutputStream, err: OutputStream)
      (f: packProcessor) = {
        new Actor {
          def act = f(in, out, err)
        }.start
      }

  def repoByPath(arg: String, user: Option[UserDoc] = None) = {
    arg match { 
      case Repo1(userName, repoName) => RepositoryDoc.byUserLoginAndRepoName(userName, repoName)
  
      case Repo2(repoName) => user.flatMap(_.repos.filter(_.name.get == repoName).headOption)
  
      case _ => None
    }
  }

  def uploadPack(r: RepositoryDoc) = { r.git.upload_pack.upload _ }

  def receivePack(r: RepositoryDoc) = {
    import scala.collection.JavaConversions._
    import notification.client._
    
    val receivePack = r.git.receive_pack

    val oldHeads = r.git.refsHeads.map(ref => (ref.getName, ref)).toMap
    val ident = receivePack.getRefLogIdent //TODO fill this
 
    // he-he hide a real call
    (in: InputStream, out: OutputStream, err: OutputStream) => {
        receivePack.receive(in, out, err)
        NotifyActor ! PushEvent(r, ident, oldHeads)
    }
  }

  val ident = """[0-9a-zA-Z\.-]+"""
  
  val Repo1 = """'?/?(%s)/(%s)'?""".format(ident, ident).r
  val Repo2 =  """'?/?(%s)'?""".format(ident).r
}