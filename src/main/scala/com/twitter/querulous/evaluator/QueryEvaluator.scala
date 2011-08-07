/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package com.twitter.querulous.evaluator

import java.sql.ResultSet
import com.twitter.conversions.time._
import com.twitter.querulous.database.ApachePoolingDatabaseFactory
import com.twitter.querulous.query.{QueryClass, Query, SqlQueryFactory}
import com.twitter.querulous.config.Connection

object QueryEvaluator extends QueryEvaluatorFactory {
  private def createEvaluatorFactory = {
    val queryFactory = new SqlQueryFactory
    val databaseFactory = new ApachePoolingDatabaseFactory(10, 10, 1.second, 10.millis, false, 0.seconds)
    new StandardQueryEvaluatorFactory(databaseFactory, queryFactory)
  }

  def apply(driver: String, url: String, username: String, password: String) = {
    createEvaluatorFactory(driver, url, username, password)
  }
}

trait QueryEvaluatorFactory {
  def apply(driver: String, url: String, username: String, password: String): QueryEvaluator

  def apply(connection: Connection): QueryEvaluator = apply(connection.driver, connection.url, connection.username, connection.password)
}

class ParamsApplier(query: Query) {
  def apply(params: Any*) = query.addParams(params: _*)
}

trait QueryEvaluator {
  def select[A](queryClass: QueryClass, query: String, params: Any*)(f: ResultSet => A): Seq[A]

  def select[A](query: String, params: Any*)(f: ResultSet => A): Seq[A] =
    select(QueryClass.Select, query, params: _*)(f)

  def selectOne[A](queryClass: QueryClass, query: String, params: Any*)(f: ResultSet => A): Option[A]

  def selectOne[A](query: String, params: Any*)(f: ResultSet => A): Option[A] =
    selectOne(QueryClass.Select, query, params: _*)(f)

  def count(queryClass: QueryClass, query: String, params: Any*): Int

  def count(query: String, params: Any*): Int =
    count(QueryClass.Select, query, params: _*)

  def execute(queryClass: QueryClass, query: String, params: Any*): Int

  def execute(query: String, params: Any*): Int =
    execute(QueryClass.Execute, query, params: _*)

  def executeBatch(queryClass: QueryClass, query: String)(f: ParamsApplier => Unit): Int

  def executeBatch(query: String)(f: ParamsApplier => Unit): Int =
    executeBatch(QueryClass.Execute, query)(f)

  def insert(queryClass: QueryClass, query: String, params: Any*): Unit

  def insert(query: String, params: Any*): Unit =
    insert(QueryClass.Execute, query, params: _*)

  def transaction[T](f: Transaction => T): T
}
