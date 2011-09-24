/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import java.sql.Connection
import net.liftweb.util.Props
import com.jolbox.bonecp.{BoneCP, BoneCPConfig}
import net.liftweb.common.{Loggable, Full}
import net.liftweb.db.{SuperConnection, ConnectionIdentifier, ConnectionManager}

/**
 * User: denis.bardadym
 * Date: 9/9/11
 * Time: 2:36 PM
 */

object DefaultConnectionManager extends ConnectionManager with Loggable {

  Class.forName(Props.get("db.driver", "org.h2.Driver"))

  private val config = new BoneCPConfig
  config.setJdbcUrl(Props.get("db.url", "jdbc:h2:~/test"))
  config.setUsername(Props.get("db.username", "sa"))
  config.setPassword(Props.get("db.password", ""))
  config.setPartitionCount(2)
  config.setMaxConnectionsPerPartition(5)
  config.setMinConnectionsPerPartition(1)

  private val pool = new BoneCP(config)

  def newConnection(name: ConnectionIdentifier) = {
    logger.trace("Try to get connection for " + name.jndiName)
    Full(pool.getConnection)
  }

  def releaseConnection(conn: Connection) {
    conn.close()
  }

  def close = pool.close()

  override def newSuperConnection(name: ConnectionIdentifier) = {
    newConnection(name).map(c => new SuperConnection(c : Connection, () => releaseConnection(c)))
  }
}