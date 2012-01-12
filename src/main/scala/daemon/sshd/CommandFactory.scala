/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package daemon.sshd

import org.apache.sshd.server.{Command => SshCommand, CommandFactory => SshCommandFactory}
import net.liftweb.common.Loggable


class CommandFactory extends SshCommandFactory with daemon.Resolver with Loggable {
  /**
   * Create a command with the given name.
   * If the command is not known, a dummy command should be returned to allow
   * the display output to be sent back to the client.
   *
   * @param command
   * @return a non null <code>Command</code>
   */
  def createCommand(command: String): SshCommand = {
    logger.debug("Receive ssh command: " + command)

    command.split(' ').toList match {
      case GIT_UPLOAD_PACK :: repoPath :: Nil => new UploadPackCommand(repoPath)
                
      case GIT_RECEIVE_PACK :: repoPath :: Nil => new ReceivePackCommand(repoPath)
      case c => {
        logger.warn("Unrecognized command: %s".format(c))
        new UnrecognizedCommand
      }
    }
  }
}
