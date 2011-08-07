/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package com.twitter.querulous.database

import com.jolbox.bonecp.{BoneCP, BoneCPConfig}

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 07.08.11
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */

class BoneCPPoolingDatabaseFactory(partitionCount: Int, maxConnectionsPerPartition: Int, minConnectionsPerPartition: Int, acquireIncrement: Int) extends DatabaseFactory {
  def apply(driver: String, url: String, username: String, password: String) =
    new BoneCPPoolingDatabase(driver, url, username, password, partitionCount, maxConnectionsPerPartition, minConnectionsPerPartition, acquireIncrement)

}

class BoneCPPoolingDatabase(
                             val driver: String,
                             val url: String,
                             val username: String,
                             password: String,
                             partitionCount: Int,
                             maxConnectionsPerPartition: Int,
                             minConnectionsPerPartition: Int,
                             acquireIncrement: Int) extends Database {
  Class.forName(driver)

  private val config = new BoneCPConfig
  config.setJdbcUrl(url)
  config.setUsername(username)
  config.setPassword(password)
  config.setMinConnectionsPerPartition(minConnectionsPerPartition)
  config.setMaxConnectionsPerPartition(maxConnectionsPerPartition)
  config.setPartitionCount(partitionCount)
  config.setAcquireIncrement(acquireIncrement)

  private val connectionPool = new BoneCP(config);

  def open() = connectionPool.getConnection();

  override def finalize() {
    connectionPool.shutdown()
  }
}