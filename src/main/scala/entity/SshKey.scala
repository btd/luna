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
              var rawValue: String,
              val repoName: Option[String]
              ) {
  private lazy val splited_key = rawValue.split(" ")

  lazy val encodedKey = splited_key(1)
  lazy val comment = splited_key(2)

  def for_user_? = repoName match {
    case None => false
    case _ => true
  }

  def for_repo_?(name: String) = repoName match {
    case None => false
    case Some(n) if (n == name) => true
  }

  def +:(trn: Transaction) = {
    trn.execute("insert into ssh_keys(owner_login, raw_value) values (?, ?)", ownerLogin, rawValue) //TODO
  }
}

object SshKey {

  def of(user: User) = DAO.select("select owner_login, raw_value, repo_name from ssh_keys where owner_login = ?", user.login) {
    //TODO
    row =>
      new SshKey(row.getString("owner_login"), row.getString("raw_value"), if (row.getString("repo_name") == null) None else Some(row.getString("repo_name")))
  }

  def of(repo: Repository) = DAO.select("SELECT owner_login, raw_value, repo_name FROM ssh_keys WHERE owner_login = ? and repo_name = ?", repo.ownerId, repo.name) {
    row =>
      new SshKey(row.getString("owner_login"), row.getString("raw_value"), Some(row.getString("repo_name")))
  }

  def add(key: SshKey, repo: Repository) = {
    DAO.execute("insert into ssh_keys(owner_login, raw_value, repo_name) values (?, ?, ?)", repo.ownerId, key.rawValue, repo.name)
  }

}