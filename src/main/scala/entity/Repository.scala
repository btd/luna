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
import java.io.File
import net.liftweb.common.{Loggable, Empty, Full, Box}

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
                  ) extends Loggable {
  def this(name: String, isOpen: Boolean, ownerId: String) = this (Repository.generateFsName(name, ownerId), name, isOpen, ownerId)

  def +:(trn : Transaction) = {
    trn.execute("insert into repositories(fs_name, name, is_open, owner_login) values (?, ?, ?, ?)",
      fsName, name, if (isOpen) 1 else 0, ownerId)
  }

  lazy val collaborators = Collaborator.of(this)

  lazy val keys = SshKey.of(this)

  def addCollaborator(user: User) =  {
     Collaborator.add(user, this)

  }

  def addKey(key: SshKey) = {
    SshKey.add(key, this)
  }


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
    //logger.debug("Path is " + fsPath)
    val dir = FileKey.resolve(new File(fsPath), FS.DETECTED)
    if (dir != null) Full(dir) else Empty
  }

  private lazy val loc = FileKey.lenient(new File(fsPath), FS.DETECTED)

  lazy val fsPath = Main.repoDir + fsName

  //  ownerId + name уникальны  ?

  lazy val publicGitUrl = "git://" + S.hostName + "/" + ownerId + "/" + name

  lazy val privateSshUrl = ownerId + "@" + S.hostName + ":" + name

  def privateSshUrl(user: User) = user.login + "@" + S.hostName + ":" + ownerId + "/" + name

  def canPush_?(user: Box[User]) = {
    user match {
      case Full(u) if u.login == ownerId => true  //владелец
      case _ => false
    }
  }

}

object Repository {

  def of(user: User) =
    DAO.select("select fs_name, name, is_open from repositories where owner_login = ?", user.login) {
      row => new Repository(row.getString("fs_name"), row.getString("name"), row.getInt("is_open") == 1, user.login)
    }

  def collaborated(user: User) = Collaborator.collaborator(user).flatMap(_.repo)


  private[entity] def generateFsName(name: String, ownerId: String) = {
    DigestUtils.sha(name + ownerId).toString
  }
}