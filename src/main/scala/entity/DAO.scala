/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import com.twitter.util.Eval
import com.twitter.querulous.config.Connection
import java.io.File
import com.twitter.querulous.query.QueryClass
import java.sql.ResultSet
import com.twitter.querulous.evaluator.{Transaction, ParamsApplier, QueryEvaluator}

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 07.08.11
 * Time: 13:02
 * To change this template use File | Settings | File Templates.
 */

//this object will read db properties from 'config/db.scala'
object DAO extends QueryEvaluator {
  private val eval = new Eval
  private val connection = eval[Connection](new File("config/db.scala"))
  private val evaluator = QueryEvaluator(connection)

  def transaction[T](f: (Transaction) => T) = evaluator.transaction[T](f)

  def insert(queryClass: QueryClass, query: String, params: Any*) = evaluator.insert(queryClass, query, params)

  def executeBatch(queryClass: QueryClass, query: String)(f: (ParamsApplier) => Unit) = evaluator.executeBatch(queryClass, query)(f)

  def execute(queryClass: QueryClass, query: String, params: Any*) = evaluator.execute(queryClass, query, params)

  def count(queryClass: QueryClass, query: String, params: Any*) = evaluator.count(queryClass, query, params)

  def selectOne[A](queryClass: QueryClass, query: String, params: Any*)(f: (ResultSet) => A) =
    evaluator.selectOne[A](queryClass, query, params)(f)

  def select[A](queryClass: QueryClass, query: String, params: Any*)(f: (ResultSet) => A) =
    evaluator.select[A](queryClass, query, params)(f)
}