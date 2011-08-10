/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd

import git.{Receive, Upload}
import org.apache.sshd.server.{Command, CommandFactory}
import com.twitter.logging.Logger
import java.lang.Exception

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */

class GitoChtoToCommandFactory extends CommandFactory {
  private val log = Logger.get(this.getClass)
  /**
   * Create a command with the given name.
   * If the command is not known, a dummy command should be returned to allow
   * the display output to be sent back to the client.
   *
   * @param command
   * @return a non null <code>Command</code>
   */
  def createCommand(command: String): Command = {
    val args: List[String] = command.split(' ').toList
    log.debug("Receive command: %s", command)
    args match {
      case "git-upload-pack" :: repoPath :: Nil => Upload(preparePath(repoPath))
      case "git-receive-pack" :: repoPath :: Nil => Receive(preparePath(repoPath))
      case _ => throw new NoSuchCommand("Not recognized command: " + command) //может быть лучше сделать DoNothing команду
    }
  }

  def preparePath(path: String) = {
    val resultedPath = if ((path startsWith "'") && (path endsWith "'"))
      path substring (1, path.length - 1)
    else path
    if (resultedPath startsWith "/") resultedPath substring 1
    else resultedPath
  }

}

class NoSuchCommand(msg: String) extends Exception {
  override def getMessage = msg
}