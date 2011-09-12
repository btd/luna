/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import java.sql.{ResultSet, Types, PreparedStatement}
import net.liftweb.db.{DefaultConnectionIdentifier, DB}
import collection.mutable.ArrayBuffer
import net.liftweb.common.Loggable
import javax.naming.{Context, InitialContext}
import javax.sql.DataSource

/**
 * User: denis.bardadym
 * Date: 9/9/11
 * Time: 11:48 AM
 */

object DAO extends Loggable {
  private def setPreparedParams(ps: PreparedStatement, params: List[Any]): PreparedStatement = {
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
    DB.use(DefaultConnectionIdentifier) {
      DB.prepareStatement(query, _) {
        ps =>
          val rs = setPreparedParams(ps, params.toList).executeQuery()

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
  }

  def selectOne[A](query: String, params: Any*)(f: ResultSet => A) = {
    select(query, params: _*)(f).headOption
  }

}