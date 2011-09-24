/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import net.liftweb.http.S
import org.apache.commons.codec.digest.DigestUtils
import org.eclipse.jgit.lib.RepositoryCache
import main.Main
import org.eclipse.jgit.lib.RepositoryCache.FileKey
import org.eclipse.jgit.util.FS
import net.liftweb.common.{Empty, Full, Box}
import java.io.File

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
  def this(name: String, isOpen: Boolean, ownerId: String) = this (Repository.generateFsName(name, ownerId), name, isOpen, ownerId)



  lazy val git = {
    dir match {
      case Full(dir) => {
        RepositoryCache.open(loc);
      }
      case Empty => {
        val repo = RepositoryCache.open(loc, false)
        repo.create(true /* bare */);
        repo
      }
    }
  }

  def dir = {
    val dir = FileKey.resolve(new File(fsPath), FS.DETECTED)
    if (dir != null) Full(dir) else Empty
  }

  private lazy val loc = FileKey.lenient(new File(fsPath), FS.DETECTED)

  lazy val fsPath = Main.repoDir + fsName

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


  def generateFsName(name: String, ownerId: String) = {
    DigestUtils.sha(name + ownerId).toString
  }
}