/*
 * Copyright (c) 2011 Denis Bardadym
 * This project like github.
 * Distributed under Apache Licence.
 */

package sshd

import org.apache.sshd.server.{Command, CommandFactory}

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */

class GitoChtoToCommandFactory extends CommandFactory {
  /**
   * Create a command with the given name.
   * If the command is not known, a dummy command should be returned to allow
   * the display output to be sent back to the client.
   *
   * @param command
   * @return a non null <code>Command</code>
   */
  def createCommand(command: String): Command = command match {
    //case "git upload-pack" => UploadCommand()
    //case "git receive-pack" => ReceiveCommand()
    case _ => throw new NoSuchCommandException(command + " doesn't supported by this server")
  }
}