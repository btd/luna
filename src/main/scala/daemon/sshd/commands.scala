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
package daemon.sshd

import notification.client._
import java.io.{OutputStream, InputStream}
import scala.actors.Actor
import org.eclipse.jgit.transport.{ReceivePack, UploadPack}
import net.liftweb.common._
import org.apache.sshd.server.{SessionAware, Environment, ExitCallback, Command => SshCommand}
import org.apache.sshd.server.session.ServerSession

import org.eclipse.jgit.lib.{Repository => JRepository, Constants}
import code.model._

trait CommandBase extends SshCommand with SessionAware with Loggable with daemon.Resolver {

  val repoPath: String

  protected var in: InputStream = _
  protected var out: OutputStream = _
  protected var err: OutputStream = _

  protected var callback: ExitCallback = _

  protected var onExit: Int = EXIT_SUCCESS

  protected var user: UserDoc = _
  protected var keys: Seq[SshKeyBase[_]] = _

  val EXIT_SUCCESS = 0
  val EXIT_ERROR = 127


  def setSession(session: ServerSession) {
    keys = session.getAttribute(DatabasePubKeyAuth.SSH_KEYS_KEY)
    user = session.getAttribute(DatabasePubKeyAuth.USER_KEY)
  }

  def setInputStream(in: InputStream) {
    this.in = in
  }

  def destroy() {}

  def setExitCallback(callback: ExitCallback) {
    this.callback = callback
  }

  def start(env: Environment) = {
    Actor.actor {
        try {
          run(env)
        } finally {
          try {
            out.flush
          } catch {
            case _ =>
          }
          try {
            err.flush
          } catch {
            case _ =>
          }
          callback.onExit(onExit)
        }
      
    }
  }


  def run(env: Environment)

  def setErrorStream(err: OutputStream) {
    this.err = err
  }

  def setOutputStream(out: OutputStream) {
    this.out = out
  }

  protected def sendError(message: String) {
    err.write(Constants.encode(message + "\n"))
    err.flush
    onExit = EXIT_ERROR
  }

  def checkRepositoryAccess(r: RepositoryDoc) = {
    if(user.id.get == r.ownerId.get) { // user@server:repo 
      !keys.filter(_.acceptableFor(r)).isEmpty
    } else {// cuser@server:user/repo
      !r.collaborators.objs.filter(_.login.get == user.login.get).isEmpty
    }
  }
}

class UploadPackCommand(val repoPath: String) extends CommandBase {
  logger.debug("Upload pack command executed")
  def run(env: Environment) = {
    for {
      repo <- repoByPath(repoPath, Some(user))
      if checkRepositoryAccess(repo)
    } {
       val up = uploadPack(repo)
       up(in, out, err)
       repo.git.fs_repo_!.close
    } 
  }
 
}

class ReceivePackCommand(val repoPath: String) extends CommandBase {
  logger.debug("Receive pack command executed")
  def run(env: Environment) = {
    for {
      repo <- repoByPath(repoPath, Some(user))
      if checkRepositoryAccess(repo)
    } {
       val up = receivePack(repo)
       up(in, out, err)
       repo.git.fs_repo_!.close
    }
  }

  
}



class UnrecognizedCommand extends CommandBase {
  val repoPath = ""

  def run(env: Environment) = {
    sendError("This command doesn't supported by this server");
  }
}