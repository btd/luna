/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd.git

import org.apache.sshd.server.{Environment, ExitCallback, Command}
import java.io.{OutputStream, InputStream}
import actors.Actor
import org.eclipse.jgit.transport.{ReceivePack, UploadPack}
import net.liftweb.common.Loggable
import org.eclipse.jgit.lib.Constants
import entity.Repository

abstract sealed class AbstractCommand extends Command with Loggable {

  protected var in: InputStream = null
  protected var out: OutputStream = null
  protected var err: OutputStream = null

  protected var callback: ExitCallback = null

  protected var onExit: Int = EXIT_SUCCESS

  val EXIT_SUCCESS = 0
  val EXIT_ERROR = 127

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

  protected def error(message: String) {
    err.write(Constants.encode(message + "\n"));
    err.flush();
    onExit = EXIT_ERROR
  }
}


case class Upload(repoPath: String) extends AbstractCommand {
  def run(env: Environment) = {
    val username = env.getEnv.get(Environment.ENV_USER)
    //TODO может быть это не эффективно?
    Repository.ownedBy(username).filter(_.name == repoPath).headOption match {
      case Some(r) => {
        logger.debug("%s try to upload pack in %s".format(username, repoPath))
        new UploadPack(r.git).upload(in, out, err)}
      case None => error("Repository not founded")
    }
  }
}

case class Receive(repoPath: String) extends AbstractCommand {
  def run(env: Environment) = {
    val username = env.getEnv.get(Environment.ENV_USER)
    //TODO может быть это не эффективно?
    Repository.ownedBy(username).filter(_.name == repoPath).headOption match {
      case Some(r) => {
        logger.debug("%s try to receive pack in %s".format(username, repoPath))
        val rp = new ReceivePack(r.git)

        rp.setAllowCreates(true)
        rp.setAllowDeletes(true)
        rp.setAllowNonFastForwards(true)
        rp.setCheckReceivedObjects(true)

        rp.receive(in, out, err)
      }
      case None => error("Repository not founded")
    }


  }
}

case class UnRecognizedCommand() extends AbstractCommand {
  def run(env: Environment) = {
    error("This command doesn't supported by this server");
  }
}