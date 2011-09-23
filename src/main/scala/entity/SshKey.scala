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
              val ownerLogin: String,
              var rawValue: String
              ) {
  lazy val encodedKey = rawValue.split(" ")(1)

  def +:(trn : Transaction) = {
    trn.execute("insert into ssh_keys(owner_login, raw_value) values (?, ?)", ownerLogin, rawValue)
  }
}

object SshKey {
  def all =
    DAO.select("SELECT owner_login, raw_value FROM ssh_keys") {
      row =>
        new SshKey(row.getString("owner_login"), row.getString("raw_value"))
    }

  def ownerBy(login: String) = DAO.select("select owner_login, raw_value from ssh_keys where owner_login = ?", login) {
    row =>
      new SshKey(row.getString("owner_login"), row.getString("raw_value"))
  }


}