/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package daemon.git

import net.liftweb._
import util._
import Helpers._
import common._
import code.model.{RepositoryDoc, UserDoc }
import main.Constants

import java.net.InetSocketAddress
import java.io._

import actors.Actor //TODO ExecutorService

import org.eclipse.jgit.lib.Repository 
import org.eclipse.jgit.transport._

import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.filter.codec._
import org.apache.mina.core.service._
import org.apache.mina.core.session._
import org.apache.mina.core.buffer._
import org.apache.mina.handler.stream._



/**
 * User: denis.bardadym
 * Date: 9/26/11
 * Time: 3:07 PM
 */

object GitDaemon extends daemon.Service with Loggable {

  def init() {
    acceptor.getFilterChain.addLast( "logger", new LoggingFilter )

    acceptor.setHandler( new GitServerHandler )

    acceptor.setReuseAddress(true)

    val port = Props.getInt(Constants.GITD_PORT_OPTION, DEFAULT_PORT)
    new Actor {
      def act = {
        logger.debug("Git daemon started on port %s".format(port))
        acceptor.bind(new InetSocketAddress(port))
      }
    }.start    
    
  }

  def shutdown() = acceptor.unbind

  val DEFAULT_PORT = 9418

  private val acceptor = new NioSocketAcceptor

}

class GitServerHandler extends StreamIoHandler with Resolver with Loggable
{
    override def processStreamIo(session:IoSession, in: InputStream, out: OutputStream ) {
      new Actor {
        def act = {
                  
          var cmd = new PacketLineIn(in).readStringRaw
          
          var nul = cmd.indexOf('\0')
          if (nul >= 0) {
            // Newer clients hide a "host" header behind this byte.
            // Currently we don't use it for anything, so we ignore
            // this portion of the command.
            cmd = cmd.substring(0, nul)
          }
          logger.debug("Read command %s".format(cmd))

          cmd.trim.split(" ").toList match {
            case GIT_UPLOAD_PACK :: path :: Nil => 
              for(proc <- packProcessing(repoByPath(path), uploadPack)) {
                logger.debug(proc)
                proc(in, out, null)
              }

            case _ => logger.debug("This command is not supported")
          }  
        }
      }.start        
    }
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
        logger.debug("Get parallel version of processor")
       (in: InputStream, out: OutputStream, err: OutputStream) => inParallel(in, out, err)(processor(repo))
    }
  }  

  private def inParallel(in: InputStream, out: OutputStream, err: OutputStream)
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

  val uploadPack = (r: RepositoryDoc) => {logger.debug("Uploading"); r.git.upload_pack.upload _}
  
  val Repo1 = """'?/?([a-zA-Z\.-]+)/([a-zA-Z\.-]+)'?""".r
  val Repo2 =  """'?/?([a-zA-Z\.-]+)'?""".r
}