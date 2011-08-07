/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package com.twitter.querulous.database

import java.sql.{SQLException, Connection}
import org.apache.commons.pool.impl.GenericObjectPool
import com.twitter.util.Duration
import org.apache.commons.dbcp.{PoolableConnectionFactory, DriverManagerConnectionFactory, PoolingDataSource}

class ApachePoolingDatabaseFactory(
                                    val minOpenConnections: Int,
                                    val maxOpenConnections: Int,
                                    checkConnectionHealthWhenIdleFor: Duration,
                                    maxWaitForConnectionReservation: Duration,
                                    checkConnectionHealthOnReservation: Boolean,
                                    evictConnectionIfIdleFor: Duration) extends DatabaseFactory {


  def apply(driver: String, url: String, username: String, password: String) = {
    new ApachePoolingDatabase(
      driver,
      url,
      username,
      password,
      minOpenConnections,
      maxOpenConnections,
      checkConnectionHealthWhenIdleFor,
      maxWaitForConnectionReservation,
      checkConnectionHealthOnReservation,
      evictConnectionIfIdleFor
    )
  }
}

class ApachePoolingDatabase(
                             val driver: String,
                             val url: String,
                             val username: String,
                             password: String,
                             minOpenConnections: Int,
                             maxOpenConnections: Int,
                             checkConnectionHealthWhenIdleFor: Duration,
                             val openTimeout: Duration,
                             checkConnectionHealthOnReservation: Boolean,
                             evictConnectionIfIdleFor: Duration)
  extends Database {

  Class.forName(driver)

  private val config = new GenericObjectPool.Config
  config.maxActive = maxOpenConnections
  config.maxIdle = maxOpenConnections
  config.minIdle = minOpenConnections
  config.maxWait = openTimeout.inMillis

  config.timeBetweenEvictionRunsMillis = checkConnectionHealthWhenIdleFor.inMillis
  config.testWhileIdle = false
  config.testOnBorrow = checkConnectionHealthOnReservation
  config.minEvictableIdleTimeMillis = evictConnectionIfIdleFor.inMillis

  config.lifo = false

  private val connectionPool = new GenericObjectPool(null, config)
  private val connectionFactory = new DriverManagerConnectionFactory(url, username, password)
  private val poolableConnectionFactory = new PoolableConnectionFactory(
    connectionFactory,
    connectionPool,
    null,
    "/* ping */ SELECT 1",
    false,
    true)
  private val poolingDataSource = new PoolingDataSource(connectionPool)
  poolingDataSource.setAccessToUnderlyingConnectionAllowed(true)



  def open() = poolingDataSource.getConnection()

  override def toString = url
}
