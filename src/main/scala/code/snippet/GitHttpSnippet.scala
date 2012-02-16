package code.snippet

import net.liftweb._
import http._
import common._
import util._
import Helpers._


import rest._

import daemon.Resolver
import code.model._

import org.eclipse.jgit.util.TemporaryBuffer
import org.eclipse.jgit.util.HttpSupport._
import org.eclipse.jgit.transport._
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser
import org.eclipse.jgit.revwalk.RevWalk

import java.io._

trait Pack {
	val repo: RepositoryDoc

	def sendInfoRefs(out: PacketLineOut): Unit

	def sendPack(in: InputStream, out: OutputStream, err: OutputStream): Unit
}

class UploadPack(val repo: RepositoryDoc, twoWay: Boolean = true) extends Pack {
	private val p = repo.git.upload_pack
	p.setBiDirectionalPipe(twoWay)

	def sendInfoRefs(out: PacketLineOut) {
		try {								
			p.sendAdvertisedRefs(new PacketLineOutRefAdvertiser(out))
		} finally {
			p.getRevWalk.release
		}
	}

	def sendPack(in: InputStream, out: OutputStream, err: OutputStream) {
		try {
			p.upload(in, out, err)
		} catch {
			case e: IOException => 
		}
	}
}

class ReceivePack(val repo: RepositoryDoc, twoWay: Boolean = true) extends Pack {
	private val p = repo.git.receive_pack
	p.setBiDirectionalPipe(twoWay)

	def sendInfoRefs(out: PacketLineOut) {
		try {								
			p.sendAdvertisedRefs(new PacketLineOutRefAdvertiser(out))
		} finally {
			p.getRevWalk.release
		}
	}

	def sendPack(in: InputStream, out: OutputStream, err: OutputStream) {
		try {
			p.receive(in, out, err)
		} catch {
			case e: IOException => 
		}
	}
}

object GitHttpSnippet extends Loggable with RestHelper with Resolver {

	private val svcPackMap = Map[String, RepositoryDoc => Pack](
		(GIT_UPLOAD_PACK -> ((r:RepositoryDoc) => new UploadPack(r, false))),
		(GIT_RECEIVE_PACK -> ((r:RepositoryDoc) => new ReceivePack(r, false))))


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

}
/*
trait Gzipped {
	self: SmartOutputStream => 

	def buffer(): TemporaryBuffer = {
		import java.util.zip.GZIPOutputStream

		var out = self.buffer
		if (256 < out.length) {
			val gzbuf = new TemporaryBuffer.Heap(LIMIT)
			try {
				val gzip = new GZIPOutputStream(gzbuf)
				try {
					out.writeTo(gzip, null)
				} finally {
					gzip.close
				}
				if (gzbuf.length() < out.length()) {
					out = gzbuf
				}
			} catch {
				case e: IOException => 
				// Most likely caused by overflowing the buffer, meaning
				// its larger if it were compressed. Discard compressed
				// copy and use the original.
			}
		}
		out
	}
}*/