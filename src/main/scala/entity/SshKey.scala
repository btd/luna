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
              val ownerId: String,
              var value: String
              ) {
  lazy val encodedKey = value.split(" ")(1)
}

object SshKey {
  def all =
    DAO.select("SELECT owner_id, value FROM ssh_keys") {
      row =>
        new SshKey(row.getString("owner_id"), row.getString("value"))
    }

  def byOwnerId(id: String) = DAO.select("SELECT owner_id, value FROM ssh_keys WHERE owner_id = ?", id) {
    row =>
      new SshKey(row.getString("owner_id"), row.getString("value"))
  }

  def byOwnerName(name: String) = DAO.select("select s.owner_id, s.value from ssh_key s inner join account a  on s.owner_id = a.email where a.name = ?", name) {
    row =>
      new SshKey(row.getString("owner_id"), row.getString("value"))
  }
}