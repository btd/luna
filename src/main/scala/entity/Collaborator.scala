/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

/**
 * User: denis.bardadym
 * Date: 9/28/11
 * Time: 10:21 AM
 */

class Collaborator(val login: String,
                   val ownerLogin: String,
                   val ownerRepoName: String) {

  lazy val user = User.withLogin(login)

  lazy val repo = user match {
    case Some(u) => Repository.of(u).filter(_.name == ownerRepoName).headOption
    case _ => None
  }

}

object Collaborator {
  def of(repo: Repository) = {
      DAO.select("SELECT user_login, owner_login, repo_name FROM collaborators WHERE owner_login = ? and repo_name = ?", repo.ownerId, repo.name) {
        row => new Collaborator(row.getString("user_login"), row.getString("owner_login"), row.getString("repo_name"))
      }
  }

  def add(user: User, repo: Repository) = {
    DAO.execute("insert into collaborators(user_login, owner_login, repo_name) values (?, ?, ?)", user.login, repo.ownerId, repo.name)
  }
}
