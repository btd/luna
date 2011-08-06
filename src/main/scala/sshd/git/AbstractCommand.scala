/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd.git

import org.apache.sshd.server.{Environment, ExitCallback, Command}
import org.eclipse.jgit.transport.UploadPack
import org.eclipse.jgit.lib.RepositoryCache.FileKey
import server.Server
import org.eclipse.jgit.util.FS
import org.eclipse.jgit.lib.{Repository, RepositoryCache}
import java.io.{File, OutputStream, InputStream}
import com.twitter.logging.Logger
import actors.Actor

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 06.08.11
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */

abstract sealed class AbstractCommand extends Command {
  protected val log = Logger.get(this.getClass)

  protected var in: InputStream = null
  protected var out: OutputStream = null
  protected var err: OutputStream = null

  protected var callback: ExitCallback = null

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
          callback.onExit(127);
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


}

case class Upload(repoPath: String) extends AbstractCommand {
  def run(env: Environment) = {
    val repo: Repository = RepositoryCache.open(
      FileKey.lenient(new File(Server.repoDir + repoPath), FS.DETECTED))
    val up = new UploadPack(repo)
    up.upload(in, out, err)
  }
}

case class Receive(repoPath: String) extends AbstractCommand {
  def run(env: Environment) = {
    //UploadPack up = new UploadPack()
  }
}