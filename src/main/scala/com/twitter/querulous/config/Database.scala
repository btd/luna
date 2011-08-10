/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package com.twitter.querulous.config

/*
 * Copyright (c) 2011 Denis Bardadym
 * This project like github.
 * Distributed under Apache Licence.
 */

import com.twitter.util.Duration
import com.twitter.conversions.time._
import com.twitter.querulous.database.{BoneCPPoolingDatabaseFactory, DatabaseFactory, ApachePoolingDatabaseFactory}

trait PoolingDatabase {
  def apply(): DatabaseFactory
}

class ApachePoolingDatabase extends PoolingDatabase {
  var sizeMin: Int = 10
  var sizeMax: Int = 10
  var testIdle: Duration = 1.second
  var maxWait: Duration = 10.millis
  var minEvictableIdle: Duration = 60.seconds
  var testOnBorrow: Boolean = false

  def apply() = {
    new ApachePoolingDatabaseFactory(
      sizeMin, sizeMax, testIdle, maxWait, testOnBorrow, minEvictableIdle)
  }
}

class BoneCPPoolingDatabase extends PoolingDatabase {
  var partitionCount: Int = 2
  var maxConnectionsPerPartition: Int = 5
  var minConnectionsPerPartition: Int = 1
  var acquireIncrement: Int = 2

  def apply() =
    new BoneCPPoolingDatabaseFactory(
      partitionCount, maxConnectionsPerPartition, minConnectionsPerPartition, acquireIncrement)

}


trait Connection {
  def url: String

  def driver: String

  def username: String

  def password: String
}
