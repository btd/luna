/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package com.twitter.querulous.query

import java.sql.{ResultSet, Connection}

trait QueryFactory {
  def apply(connection: Connection, queryClass: QueryClass, queryString: String, params: Any*): Query
}

trait Query {
  def select[A](f: ResultSet => A): Seq[A]

  def execute(): Int

  def addParams(params: Any*)

  def cancel()
}
