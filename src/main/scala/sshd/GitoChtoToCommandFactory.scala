/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd

import git.{Receive, Upload}
import org.apache.sshd.server.{Command, CommandFactory}
import com.twitter.logging.Logger

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
      //case "git upload-pack" => UploadCommand()
      //case "git receive-pack" => ReceiveCommand()
      case _ => log.warning("Not recognized command"); throw new NoSuchCommandException(command + " doesn't supported by this server")
    }
  }

  def preparePath(path: String) =
    if ((path startsWith "'") && (path endsWith "'"))
      path substring (1, path.length - 1)
    else path

}