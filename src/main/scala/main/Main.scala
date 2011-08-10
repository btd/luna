/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package main

import sshd.SshDaemon
import com.twitter.util.Eval
import java.io.File
import config.Server
import com.twitter.querulous.config.{BoneCPPoolingDatabase, Connection}

object Main extends Application {
  val dbConnectionFilePath = "config/db.scala"
  val serverOptionsFilePath = "config/server.scala"
  val dbPoolFilePath = "config/pool.scala"

  private val eval = new Eval

  def ifFileNotExists[A](file: File)(other: => A) = {
    if (file.exists) eval[A](file) else other
  }
  def createSchema = {}

  val connection = ifFileNotExists(new File(dbConnectionFilePath)) {
    createSchema
    new Connection {
      def url = "jdbc:h2:gitochtoto"

      def username = "sa"

      def driver = "org.h2.Driver"

      def password = ""
    }
  }
  val pool = ifFileNotExists(new File(dbPoolFilePath)) {
    new BoneCPPoolingDatabase {}
  }

  val server = ifFileNotExists(new File(serverOptionsFilePath)) {
    new Server {}
  }


  SshDaemon.start()
}