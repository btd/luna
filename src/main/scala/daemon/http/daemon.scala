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
package daemon.http

import net.liftweb.common.Loggable

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

import code.model.RepositoryDoc

import daemon.{UploadPack, ReceivePack, Resolver, Pack, Service}

import java.io._

import org.eclipse.jgit.util.TemporaryBuffer
import org.eclipse.jgit.util.HttpSupport._
import org.eclipse.jgit.transport._
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser
import org.eclipse.jgit.revwalk.RevWalk

object SmartHttpDaemon extends Service with Loggable {

  var inited = false
  val server = Http.local(9081).plan(GitSmartHttp.plan)

  def init() {
 	scala.actors.Actor.actor {
  		server.start()
  		logger.debug("Smart http daemon started on port %s".format(9081))
    	inited = true
 	}
  }

  def shutdown() {
 	server.stop()
  }

  

  def repoUrlForCurrentUser(r: RepositoryDoc) = ""

}

//object GitSmartHttpPlan extends async.Planify(GitSmartHttp.intent)

case class AdvertisedResponce(svc: String, r: RepositoryDoc, resolver: RepositoryDoc => Pack) extends ResponseStreamer with Loggable  {
	def stream(os: OutputStream) {
		logger.debug("Begin adv streaming")
		val bout = new BufferedOutputStream(os)

		val buf = new PacketLineOut(bout)
        buf.writeString("# service=" + svc + "\n")
        buf.end

        val pp = resolver(r)
        pp.sendInfoRefs(buf)

        bout.flush
        bout.close
        logger.debug("Finish adv streaming")
	}
}

case class GitPackResponce(svc: String, r: RepositoryDoc, resolver: RepositoryDoc => Pack, in: InputStream) extends ResponseStreamer with Loggable {
	def stream(os: OutputStream) { 
		val bout = new BufferedOutputStream(os)
		try {
			val pp = resolver(r)
			pp.sendPack(in, bout, null) 
		} catch {
			case e: Throwable => logger.warn("Exception occured, while " + svc, e)
		} finally {
			bout.flush
        	bout.close
		}
	}
}


object GitSmartHttp extends daemon.Resolver with Loggable {
  private val svcPackMap = Map[String, RepositoryDoc => Pack](
		(GIT_UPLOAD_PACK -> ((r:RepositoryDoc) => new UploadPack(r, false))),
		(GIT_RECEIVE_PACK -> ((r:RepositoryDoc) => new ReceivePack(r, false))))

  val plan = async.Planify {
  	case GET(Path(Seg(userName :: Repo(repoName) :: "info" :: "refs" :: Nil))) & Params(Svc(svc)) => {
  		logger.debug("Try to send advertisement for %s".format(svc))
  		svcPackMap.get(svc) match {
  			case Some(resolver) => 
  				RepositoryDoc.byUserLoginAndRepoName(userName, repoName.substring(0, repoName.length - 4)) match {
					case Some(repo) => AdvertisedResponce(svc, repo, resolver) ~> ContentType("application/x-" + svc + "-advertisement")
					case _ => Forbidden ~> ResponseString("No such repo")
				}
				
			case _ => Forbidden ~> ResponseString("No such svc")
  		}
  	}
  	case req @ POST(Path(Seg(userName :: Repo(repoName) :: GitService(svc) :: Nil))) 
  		if RequestContentType(req).filter(_ == "application/x-" + svc + "-request").isEmpty => {
		logger.debug("Try to send pack for %s".format(svc))
		svcPackMap.get(svc) match {
  			case Some(resolver) => 
  				RepositoryDoc.byUserLoginAndRepoName(userName, repoName.substring(0, repoName.length - 4)) match {
					case Some(repo) => GitPackResponce(svc, repo, resolver, req.inputStream) ~> ContentType("application/x-" + svc + "-result")
					case _ => Forbidden ~> ResponseString("No such repo")
				}
				
			case _ => Forbidden ~> ResponseString("No such svc")
  		}
  	}
			
    case _ => Pass
  }
}

object Repo {
	def unapply(name: String) = if (name.endsWith(".git")) Some(name) else None
}

object Svc extends Params.Extract(
  "service",
  Params.first ~> Params.pred(GitService.isGitPackName)
)

object GitService {
	def isGitPackName(name: String) = name == GitSmartHttp.GIT_UPLOAD_PACK || name == GitSmartHttp.GIT_RECEIVE_PACK
	def unapply(name: String) = Some(name).filter(isGitPackName)
}