/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import net.liftweb.http.S
import net.liftweb.common.{Full, Box}

/**
 * User: denis.bardadym
 * Date: 9/14/11
 * Time: 10:06 AM
 */

class Repository(val fsName: String, //имя папки репозитория not null unique primary key хеш наверно SHA-1
                 val name: String, //имя репозитория для пользователя not null
                 //val clonnedFrom: String, //id того репозитория откуда был склонирован
                 val isOpen: Boolean, //открытый или закрытый репозиторий not null default true
                 val ownerId: String //login владельца репозитория not null
                  ) {
  //  ownerId + name уникальны  ?

  lazy val publicGitUrl = "git://" + S.hostName + "/" + ownerId + "/" + name

  lazy val privateSshUrl = ownerId + "@" + S.hostName + ":" + name

  def canPush_?(user: Box[User]) = {
    user match {
      case Full(u) if u.login == ownerId => true //TODO когда будет добавлен множественный доступ будет весело
      case _ => false
    }
  }

}

object Repository {
  def ownedBy(login: String) =
    DAO.select("select fs_name, name, is_open from repositories where owner_login = ?", login) {
      row => new Repository(row.getString("fs_name"), row.getString("name"), row.getInt("is_open") == 1, login)
    }
}