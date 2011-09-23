/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package main

import sshd.SshDaemon
import net.liftweb.util.Props

object Main extends App {
  val repoDir = Props.get("repoDir", "C:\\repo")
  val serverName = Props.get("serverName", "localhost")

}