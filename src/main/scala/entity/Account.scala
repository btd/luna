/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity


/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */

class Account(
               val id: Int, //уникальный и not null
               val email: String, //уникальный и not null может надо будет добавить хоть какую то валидацию (н-р что там есть @)
               val name: String,
               val passwd: String // not null TODO в будущем это будет хешированный пароль
               )

object Account {
  def all = DAO.select("SELECT id, email, name, passwd FROM accounts") {
    row =>
      new Account(row.getInt("id"), row.getString("email"), row.getString("name"), row.getString("passwd"))
  }

  def byId(id: Int) = DAO.selectOne("SELECT id, email, name, passwd FROM accounts WHERE id = ?", id) {
    row =>
      new Account(row.getInt("id"), row.getString("email"), row.getString("name"), row.getString("passwd"))
  }

  def byEmail(email: String) = DAO.selectOne("SELECT id, email, name, passwd FROM accounts WHERE email = ?", email) {
    row =>
      new Account(row.getInt("id"), row.getString("email"), row.getString("name"), row.getString("passwd"))
  }
}



