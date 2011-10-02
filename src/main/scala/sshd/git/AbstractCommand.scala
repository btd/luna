/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd.git

import java.io.{OutputStream, InputStream}
import actors.Actor
import org.eclipse.jgit.transport.{ReceivePack, UploadPack}
import net.liftweb.common._
import org.apache.sshd.server.{SessionAware, Environment, ExitCallback, Command => SshCommand}
import org.apache.sshd.server.session.ServerSession
import sshd.DatabasePubKeyAuth
import org.eclipse.jgit.lib.{Repository => JRepository, Constants}
import code.model.{UserDoc, SshKeyDoc}

abstract sealed class AbstractCommand extends SshCommand with SessionAware with Loggable {

  protected var in: InputStream = null
  protected var out: OutputStream = null
  protected var err: OutputStream = null

  protected var callback: ExitCallback = null

  protected var onExit: Int = EXIT_SUCCESS

  protected var user: UserDoc = null
  protected var keys: Seq[SshKeyDoc] = null

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
          in.close();
          out.close();
          err.close();
          callback.onExit(onExit);
        }
      }

    }.start();
  }


  def run(env: Environment)

  def setErrorStream(err: OutputStream) {
    this.err = err
  }

  def setOutputStream(out: OutputStream) {
    this.out = out
  }

  protected def sendError(message: String) {
    err.write(Constants.encode(message + "\n"));
    err.flush();
    onExit = EXIT_ERROR
  }
}

case class Command[A](factory: (JRepository, InputStream, OutputStream, OutputStream) => A, repoPath: String) extends AbstractCommand {
  def run(env: Environment) = {
    repoPath.split("/").toList match {
      case repoName :: Nil => {
        user.repos.filter(_.name.get == repoName).headOption match {
          case Some(r) => {
            if (!keys.filter(_.acceptableFor_?(r)).isEmpty) {
              factory(r.git, in, out, err)
            } else sendError("You have no permisson")
          }
          case _ => sendError("Repository not founded")
        }
      }
      case userName :: repoName :: Nil => {
        UserDoc.find("login", userName) match {
          case Full(u) => {
            u.repos.filter(_.name.get == repoName).headOption match {
              case Some(r) =>
                if (!r.collaborators.filter(_.login.get == user.login.get).isEmpty) {
                  factory(r.git, in, out, err)
                } else sendError("You have no permission")
              case _ => sendError("Repository not founded")
            }
          }
          case _ => sendError("User not founded")
        }
      }
      case _ => sendError("Invalid repo address")
    }
  }


}


object Upload {

  def createPack(repo: JRepository, in: InputStream, out: OutputStream,
                 err: OutputStream) = {
    new UploadPack(repo).upload(in, out, err)
  }

}


object Receive {

  def createPack(repo: JRepository, in: InputStream, out: OutputStream,
                 err: OutputStream) = {
    val rp = new ReceivePack(repo)

    rp.setAllowCreates(true)
    rp.setAllowDeletes(true)
    rp.setAllowNonFastForwards(true)
    rp.setCheckReceivedObjects(true)

    rp.receive(in, out, err)
  }
}

case class UnRecognizedCommand() extends AbstractCommand {
  def run(env: Environment) = {
    sendError("This command doesn't supported by this server");
  }
}