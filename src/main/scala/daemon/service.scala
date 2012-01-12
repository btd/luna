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

  val uploadPack = (r: RepositoryDoc) => { r.git.upload_pack.upload _ }

  val receivePack = (r: RepositoryDoc) => {
    import scala.collection.JavaConverters._
    import notification.client._
    
    val receivePack = r.git.receive_pack
  
    //TODO
    NotifyActor ! PushEvent(r, Empty, receivePack.getRefLogIdent, () => { receivePack.getRevWalk.asScala.toList }) 
 
    receivePack.receive _ }
  
  val Repo1 = """'?/?([a-zA-Z\.-]+)/([a-zA-Z\.-]+)'?""".r
  val Repo2 =  """'?/?([a-zA-Z\.-]+)'?""".r
}