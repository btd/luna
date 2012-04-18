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
package daemon.git

import net.liftweb._
import util._
import http.S
import Helpers._
import common._
import code.model.{RepositoryDoc, UserDoc }
import main.Constants

import java.net.InetSocketAddress
import java.io._

import scala.actors.Actor //TODO ExecutorService

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

  lazy val port = Props.getInt(Constants.GITD_PORT_OPTION, DEFAULT_PORT)

  var inited = false

  def init() {
    acceptor.getFilterChain.addLast( "logger", new LoggingFilter )

    acceptor.setHandler( new GitServerHandler )

    acceptor.setReuseAddress(true)    
    
    acceptor.bind(new InetSocketAddress(port))   
    logger.debug("Git daemon started on port %s".format(port))
    inited = true
  }

  def shutdown() = acceptor.unbind

  val DEFAULT_PORT = 9418

  private val acceptor = new NioSocketAcceptor

  def repoUrlForCurrentUser(r: RepositoryDoc) = {
    "git://" + S.hostName + (if(port != DEFAULT_PORT) ":" + port else "") + "/" + r.owner.login.get + "/" + r.name.get + ".git"
  }

}

class GitServerHandler extends StreamIoHandler with daemon.Resolver with Loggable {
    override def processStreamIo(session:IoSession, in: InputStream, out: OutputStream ) {
      Actor.actor {
                  
          try {        
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
                for(proc <- packProcessing(repoByPath(path), uploadPack(_, None), (r: RepositoryDoc) => {r.open_?.get})) {
                  proc(in, out, null)
                } 

              case _ => logger.debug("This command is not supported")
            }  
          } finally {
            in.close
            out.close
            session.close(true)
          }
         
      }
    }
}

