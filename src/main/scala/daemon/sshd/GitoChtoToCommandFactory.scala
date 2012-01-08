/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package daemon.sshd

import org.apache.sshd.server.{Command => SshCommand, CommandFactory}
import net.liftweb.common.Loggable


class GitoChtoToCommandFactory extends CommandFactory with Loggable {
  /**
   * Create a command with the given name.
   * If the command is not known, a dummy command should be returned to allow
   * the display output to be sent back to the client.
   *
   * @param command
   * @return a non null <code>Command</code>
   */
  def createCommand(command: String): SshCommand = {
    val args: List[String] = command.split(' ').toList
    logger.debug("Receive command: " + command)
    args match {
      case "git-upload-pack" :: repoPath :: Nil => Command(Upload.createPack, preparePath(repoPath))
      case "git-receive-pack" :: repoPath :: Nil => Command(Receive.createPack, preparePath(repoPath))
      case c => {
        logger.warn("Unrecognized command: %s".format(c))
        UnRecognizedCommand()
      }
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
