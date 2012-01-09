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

object GitDaemon extends daemon.Service {

  def init() {
    acceptor.getFilterChain.addLast( "logger", new LoggingFilter )

    acceptor.setHandler(  new GitServerHandler(RepositoryResolver.get) )

    acceptor.setReuseAddress(true)

    acceptor.bind(new InetSocketAddress(Props.getInt(Constants.GITD_PORT_OPTION, DEFAULT_PORT)))
    
  }

  def shutdown() = acceptor.unbind

  val DEFAULT_PORT = 9418

  private val acceptor = new NioSocketAcceptor

}

class GitServerHandler(resolver: (String) => Box[RepositoryDoc]) extends StreamIoHandler with Loggable
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
              //
              cmd = cmd.substring(0, nul)
            }

            val list = cmd.trim.split(" ").toList 

            list match {
              case "git-upload-pack" :: path :: Nil => {
                resolver(path) match {
                  case Full(repo) => {
                    logger.debug("Begin upload")
                    repo.git.upload_pack.upload(in, out, null)
                  } 
                  case _ => logger.debug("No repository for this path")
                }
              }
              case _ => 
            }
          }
        }.start
        
    }
}

trait Resolver {

  self: Loggable =>

  def get(args: List[String]) = {
    args match {
      case arg :: Nil => arg match {
          case Repo1(userName, repoName) => logger.debug("get a repository %s/%s".format(userName, repoName))

          case _ => logger.debug("Unrecognized argument")
        }
      case _ => logger.debug("No arguments - no repo")
    }
  }
  val Repo1 = """(?:\s+)?'?(\w+)/(\w+)'?(?:\s+)?""".r
  val Repo2 = """(?:\s+)?'?(\w+)'?(?:\s+)?""".r
}



object RepositoryResolver extends Loggable {
  def get(name: String): Box[RepositoryDoc] = {
    logger.debug("Get anonymous request %s".format(name))

    name.split("/").toList.filter(s => !s.isEmpty) match {
      case user :: repoName :: Nil => RepositoryDoc.byUserLoginAndRepoName(user, repoName)
      case _ => Empty
    }
  }
}