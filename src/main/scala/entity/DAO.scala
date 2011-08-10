/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import com.twitter.util.Eval
import com.twitter.querulous.config.Connection
import java.io.File
import java.sql.ResultSet
import main.Main
import com.twitter.querulous.evaluator.{StandardQueryEvaluatorFactory, Transaction, ParamsApplier, QueryEvaluator}
import com.twitter.querulous.query.{SqlQueryFactory, QueryClass}

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 07.08.11
 * Time: 13:02
 * To change this template use File | Settings | File Templates.
 */

//this object will read db properties from 'config/db.scala'
object DAO extends QueryEvaluator {

  private val evaluator = (new StandardQueryEvaluatorFactory(Main.pool(), new SqlQueryFactory))(Main.connection)

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