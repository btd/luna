/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package daemon.sshd

import notification.client._
import java.io.{OutputStream, InputStream}
import actors.Actor
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
    new Actor {

      def act() {
        try {
          run(env)
        } finally {
          in.close
          out.close
          err.close
          callback.onExit(onExit)
        }
      }

    }.start
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
}

class UploadPackCommand(val repoPath: String) extends CommandBase {
  logger.debug("Upload pack command executed")
  def run(env: Environment) = {
    for(proc <- packProcessing(repoByPath(repoPath, Some(user)), uploadPack)) {
          proc(in, out, err)
    } 
  }
}

class ReceivePackCommand(val repoPath: String) extends CommandBase {
  logger.debug("Receive pack command executed")
  def run(env: Environment) = {
    for(proc <- packProcessing(repoByPath(repoPath, Some(user)), receivePack, checkRepositoryAccess)) {
          proc(in, out, err)
    } 
  }

  def checkRepositoryAccess(r: RepositoryDoc) = {
    if(user.id.get == r.ownerId.get) { // user@server:repo 
      !keys.filter(_.acceptableFor(r)).isEmpty
    } else {// cuser@server:user/repo
      !r.collaborators.filter(_.login.get == user.login.get).isEmpty
    }
  }
}



class UnrecognizedCommand extends CommandBase {
  val repoPath = ""

  def run(env: Environment) = {
    sendError("This command doesn't supported by this server");
  }
}