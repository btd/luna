/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import net.liftweb.json.JsonDSL._
import code.model.{UserDoc, RepositoryDoc}

/**
 * User: denis.bardadym
 * Date: 9/28/11
 * Time: 10:21 AM
 */

class Collaborator(val login: String,
                   val ownerLogin: String,
                   val ownerRepoName: String) {

  lazy val owner = UserDoc.find("login", ownerLogin)

  lazy val repo = RepositoryDoc.find(("ownerLogin" -> ownerLogin) ~ ("ownerRepoName" -> ownerRepoName))
}

object Collaborator {
  def of(repo: RepositoryDoc) = {
      DAO.select("SELECT user_login, owner_login, repo_name FROM collaborators WHERE owner_login = ? and repo_name = ?", repo.ownerId.obj.get.login.get, repo.name.get) {
        row => new Collaborator(row.getString("user_login"), row.getString("owner_login"), row.getString("repo_name"))
      }
  }

  def collaborator(user: UserDoc) = {
    DAO.select("SELECT user_login, owner_login, repo_name FROM collaborators WHERE user_login = ?", user.login.get) {
        row => new Collaborator(row.getString("user_login"), row.getString("owner_login"), row.getString("repo_name"))
      }
  }

  def add(user: UserDoc, repo: RepositoryDoc) = {
    DAO.execute("insert into collaborators(user_login, owner_login, repo_name) values (?, ?, ?)", user.login.get, repo.ownerId.obj.get.login.get, repo.name.get)
  }
}
