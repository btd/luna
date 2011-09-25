/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package main

import sshd.SshDaemon
import net.liftweb.util.Props

object Main extends App {
  lazy val repoDir = Props.get("repoDir", "./repo/") //Эта хрень обязана кончаться на /
  lazy val serverName = Props.get("serverName", "localhost")

}