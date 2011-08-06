/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd.git

import java.io.{OutputStream, InputStream}
import org.apache.sshd.server.{Environment, ExitCallback, Command}
import org.eclipse.jgit.lib.Constants

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 06.08.11
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */

abstract sealed class AbstractCommand extends Command {
 private var in: InputStream = null
    private var out: OutputStream = null
    private var err: OutputStream = null

    private var callback: ExitCallback = null

    def setInputStream(in: InputStream) {
      this.in = in
    }

    def destroy() {}

    def setExitCallback(callback: ExitCallback) {
      this.callback = callback
    }

    def start(env: Environment)

    def setErrorStream(err: OutputStream) {
      this.err = err
    }

    def setOutputStream(out: OutputStream) {
      this.out = out
    }

    def parseCommandLine(args:String*): String
}

case class Upload(repo:String) extends AbstractCommand {
  def start(env: Environment) = null

  def parseCommandLine(args: String*) = null
}