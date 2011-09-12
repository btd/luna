/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

class User(
            // val id: Int, //уникальный и not null   (нахера нужех id?)
            val email: String, //уникальный и not null может надо будет добавить хоть какую то валидацию (н-р что там есть @)
            val name: String,
            val passwd: String // not null TODO в будущем это будет хешированный пароль
            )

object User {
  def all = DAO.select("SELECT  email, name, passwd FROM accounts") {
    row =>
      new User(row.getString("email"), row.getString("name"), row.getString("passwd"))
  }


  def byEmail(email: String) = DAO.selectOne("SELECT  email, name, passwd FROM accounts WHERE email = ?", email) {
    row =>
      new User(row.getString("email"), row.getString("name"), row.getString("passwd"))
  }

  var currentUser: Option[User] = None

  def loggedIn_? = currentUser match {
    case None => false
    case _ => true
  }
}



