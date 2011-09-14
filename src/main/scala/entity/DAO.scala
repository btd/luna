/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import collection.mutable.ArrayBuffer
import net.liftweb.common.Loggable
import java.sql._
import net.liftweb.db.{SuperConnection, DefaultConnectionIdentifier, DB}

/**
 * User: denis.bardadym
 * Date: 9/9/11
 * Time: 11:48 AM
 */

object DAO extends QueryEvaluator with Loggable {

  def atomic[T](f: Transaction => T) = {
    transaction {
      t =>
        f(t)
    }
  }

  private[entity] def setPreparedParams(ps: PreparedStatement, params: List[Any]): PreparedStatement = {
    params.zipWithIndex.foreach {
      case (null, idx) => ps.setNull(idx + 1, Types.VARCHAR)
      case (i: Int, idx) => ps.setInt(idx + 1, i)
      case (l: Long, idx) => ps.setLong(idx + 1, l)
      case (d: Double, idx) => ps.setDouble(idx + 1, d)
      case (f: Float, idx) => ps.setFloat(idx + 1, f)
      // Allow the user to specify how they want the Date handled based on the input type
      case (t: java.sql.Timestamp, idx) => ps.setTimestamp(idx + 1, t)
      case (d: java.sql.Date, idx) => ps.setDate(idx + 1, d)
      case (t: java.sql.Time, idx) => ps.setTime(idx + 1, t)
      /* java.util.Date has to go last, since the java.sql date/time classes subclass it. By default we
       * assume a Timestamp value */
      case (d: java.util.Date, idx) => ps.setTimestamp(idx + 1, new java.sql.Timestamp(d.getTime))
      case (b: Boolean, idx) => ps.setBoolean(idx + 1, b)
      case (s: String, idx) => ps.setString(idx + 1, s)
      case (bn: java.math.BigDecimal, idx) => ps.setBigDecimal(idx + 1, bn)
      case (obj, idx) => ps.setObject(idx + 1, obj)
    }
    ps
  }

  def select[A](query: String, params: Any*)(f: ResultSet => A): Seq[A] = {
    transaction {
      _.select(query, params: _*)(f)
    }
  }

  def selectOne[A](query: String, params: Any*)(f: ResultSet => A) = {
    transaction {
      _.selectOne(query, params: _*)(f)
    }

  }

  def execute(query: String, params: Any*) = {
    transaction {
      _.execute(query, params : _*)
    }
  }

  def transaction[T](f: Transaction => T) = {
    DB.use(DefaultConnectionIdentifier) { conn =>
        f(new Transaction(conn))
    }
  }
}

trait QueryEvaluator {
  def select[A](query: String, params: Any*)(f: ResultSet => A): Seq[A]

  def selectOne[A](query: String, params: Any*)(f: ResultSet => A): Option[A]

  def execute(query: String, params: Any*): Int

  def transaction[T](f: Transaction => T): T
}

class Transaction(connection: SuperConnection) extends QueryEvaluator {
  def select[A](query: String, params: Any*)(f: ResultSet => A) = {
    DB.prepareStatement(query, connection) {
      ps =>
        val rs = DAO.setPreparedParams(ps, params.toList).executeQuery()

        try {
          val finalResult = new ArrayBuffer[A]
          while (rs.next()) {
            finalResult += f(rs)
          }
          finalResult
        } finally {
          rs.close()
        }
    }

  }

  def selectOne[A](query: String, params: Any*)(f: ResultSet => A) = {
    select(query, params: _*)(f).headOption
  }

  def execute(query: String, params: Any*) = {
    DB.prepareStatement(query, connection) {
      ps => DAO.setPreparedParams(ps, params.toList).executeUpdate()

    }
  }

  def transaction[T](f: Transaction => T) = f(this)
}