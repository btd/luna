/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package com.twitter.querulous.database

import java.sql.Connection
import com.twitter.util.Duration

trait DatabaseFactory {
  def apply(driver: String, url: String, username: String, password: String): Database
}

trait Database {
  def driver: String

  def url: String

  def username: String

  def openTimeout: Duration

  def open(): Connection

  def close(connection: Connection)

  def withConnection[A](f: Connection => A): A = {
    val connection = open()
    try {
      f(connection)
    } finally {
      close(connection)
    }
  }

}
