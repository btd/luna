/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd.git

import net.liftweb._
import util._
import Helpers._
import common._
import code.model.{RepositoryDoc, UserDoc }

import java.net.InetSocketAddress
import java.io._
import actors.Actor

import org.eclipse.jgit.lib.Repository 
import org.eclipse.jgit.transport._

import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.filter.codec._
import org.apache.mina.core.service._
import org.apache.mina.core.session._
import org.apache.mina.core.buffer._
import org.apache.mina.handler.stream._

import com.foursquare.rogue.Rogue._



/**
 * User: denis.bardadym
 * Date: 9/26/11
 * Time: 3:07 PM
 */

object GitDaemon {
  
  private lazy val daemon = new Daemon(RepositoryResolver.get)

  def start = daemon.start

  def stop = daemon.stop
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

class Daemon(resolver: (String) => Box[RepositoryDoc]) {

  val DEFAULT_PORT = 9418

  private val acceptor = new NioSocketAcceptor

  acceptor.getFilterChain.addLast( "logger", new LoggingFilter )

  acceptor.setHandler(  new GitServerHandler(resolver) )

  acceptor.setReuseAddress(true)


  def start = acceptor.bind(new InetSocketAddress(DEFAULT_PORT))

  def stop = acceptor.unbind
  
}

object RepositoryResolver extends Loggable {
  def get(name: String): Box[RepositoryDoc] = {
    logger.debug("Get anonymous request %s".format(name))

    name.split("/").toList.filter(s => !s.isEmpty) match {
      case user :: repoName :: Nil => {
        tryo {
          (UserDoc where (_.login eqs user) get).get.repos.filter(_.name.get == repoName).head
        } or {
          Empty
        }
      }
      case _ => Empty
    }
  }
}