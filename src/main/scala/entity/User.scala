/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

class User(
            // val id: Int, //уникальный и not null   (нахера нужех id?)
            val email: String, //уникальный и not null может надо будет добавить хоть какую то валидацию (н-р что там есть @)
            val login: String,
            val password: String // not null TODO в будущем это будет хешированный пароль
            ) {
  def +:(trn : Transaction) = {
    trn.execute("insert into users(email, login, password) values (?, ?, ?)", email, login, password)
  }

  def keys = SshKey.byOwnerLogin(login)
}

object User {
  def all = DAO.select("SELECT  email, login, password FROM users") {
    row =>
      new User(row.getString("email"), row.getString("login"), row.getString("password"))
  }


  def withEmail(email: String) = DAO.selectOne("SELECT  email, login, password FROM users WHERE email = ?", email) {
    row =>
      new User(row.getString("email"), row.getString("login"), row.getString("password"))
  }

  var current: Option[User] = None

  def loggedIn_? = current match {
    case None => false
    case _ => true
  }
}



