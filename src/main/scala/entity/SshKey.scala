/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */

class SshKey(
              val ownerId: Int,
              var value: String
              )

object SshKey {
  def all = DAO.select("SELECT owner_id, value FROM ssh_keys") {
    row =>
      new SshKey(row.getInt("owner_id"), row.getString("value"))
  }

  def byOwnerId(id: Int) = DAO.selectOne("SELECT owner_id, value FROM ssh_keys WHERE owner_id = ?", id) {
    row =>
      new SshKey(row.getInt("owner_id"), row.getString("value"))
  }
}