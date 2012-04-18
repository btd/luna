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
package code.snippet

import net.liftweb._
import http._
import common._
import util._
import Helpers._


import rest._

import daemon.{UploadPack, ReceivePack, Resolver, Pack}
import code.model._

import org.eclipse.jgit.util.TemporaryBuffer
import org.eclipse.jgit.util.HttpSupport._
import org.eclipse.jgit.transport._
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser
import org.eclipse.jgit.revwalk.RevWalk

import java.io._



object GitHttpSnippet extends Loggable with RestHelper with Resolver {

	def getUser = {
		logger.debug("Try to get userId from session")
		S.getSessionAttribute("user").flatMap{ s => 
			logger.debug("Get is from session " + s)
			UserDoc.byId(new org.bson.types.ObjectId(s))
		}.toOption
	}

	private val svcPackMap = Map[String, RepositoryDoc => Pack](
		(GIT_UPLOAD_PACK -> ((r:RepositoryDoc) => new UploadPack(r, getUser, false))),
		(GIT_RECEIVE_PACK -> ((r:RepositoryDoc) => new ReceivePack(r, getUser, false))))


	private def makeAdvResponce(svc: String, r: RepositoryDoc): LiftResponse = {
		svcPackMap.get(svc) match {
			case Some(p) => {
				OutputStreamResponse((out:OutputStream) => {
					val bout = new BufferedOutputStream(out)

					val buf = new PacketLineOut(bout)
			        buf.writeString("# service=" + svc + "\n")
			        buf.end

			        val pp = p(r)
			        pp.sendInfoRefs(buf)

			        bout.flush
			        bout.close
				}, List("Content-Type" -> ("application/x-" + svc + "-advertisement")))
			}
			case _ => ForbiddenResponse("No such svc")
		}
		
	}

	private def makeResponce(svc: String, r: RepositoryDoc, in: InputStream): LiftResponse = {
		svcPackMap.get(svc) match {
			case Some(p) => 
				OutputStreamResponse((out:OutputStream) => {
					val bout = new BufferedOutputStream(out)
					try {
						val pp = p(r)
						pp.sendPack(in, bout, null) 
					} catch {
						case e: Throwable => logger.warn("Exception occured, while " + svc, e)
					} finally {
						bout.flush
			        	bout.close
					}
				}, List("Content-Type" -> ("application/x-" + svc + "-result")))
			case _ => ForbiddenResponse("No such svc")
		}
	}

	serve {
		case r @ Req(userName :: repoName :: "info" :: "refs" :: Nil, _, _) if (repoName.endsWith(".git")) => {

			for{repo <- RepositoryDoc.byUserLoginAndRepoName(userName, repoName.substring(0, repoName.length - 4))} yield {
				S.params("service") match {
					case svc :: Nil => makeAdvResponce(svc, repo)
					case _ => ForbiddenResponse("No service param")
				}
				
			}
			
		}
		case r @ Req(userName :: repoName :: svc :: Nil, _, PostRequest) 
				if (repoName.endsWith(".git") && r.contentType == "application/x-" + svc + "-request") => {
			for{repo <- RepositoryDoc.byUserLoginAndRepoName(userName, repoName.substring(0, repoName.length - 4))} yield {
				makeResponce(svc, repo, r.rawInputStream.get)	
			}
		}
	}


	def acceptsGzipEncoding(r: Req): Boolean = acceptsGzipEncoding(r.header(HDR_ACCEPT_ENCODING))

	def acceptsGzipEncoding(accepts: Box[String]): Boolean = accepts match {
		case Full(v) => v.indexOf(ENCODING_GZIP) < 0
		case _ => false
	}

	val DEFAULT_PORT = 80

	lazy val port = S.request.map(_.request.serverPort).get

	lazy val protocol = S.request.map(_.request.url.split("://")(0)).get

	def repoUrlForCurrentUser(r: RepositoryDoc):String = r.canPush_?(UserDoc.currentUser) match {
	    case true => UserDoc.currentUser.map(user => protocol + "://" + user.login.get + "@" + S.hostName + (if(port == DEFAULT_PORT) "" else ":" + port) + "/" + r.owner.login.get + "/" + r.name.get + ".git").openOr("")
	    case false  => protocol + "://" + S.hostName + (if(port == DEFAULT_PORT) "" else ":" + port) + "/" + r.owner.login.get + "/" + r.name.get + ".git"
	}

}