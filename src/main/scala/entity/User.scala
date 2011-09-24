/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

import net.liftweb._

import common._
import util._
import http._

/*
TODO
Подумать о трайтах
Entity -> all
Compisition -> ownedBy[T]
Atomic -> +:
 */

class User(
            // val id: Int, //уникальный и not null   (нахера нужех id?)
            val email: String, //уникальный и not null может надо будет добавить хоть какую то валидацию (н-р что там есть @)
            val login: String,
            val password: String // not null TODO в будущем это будет хешированный пароль
            ) {
  def +:(trn : Transaction) = {
    trn.execute("insert into users(email, login, password) values (?, ?, ?)", email, login, password)
  }

  def keys = SshKey.ownerBy(login)

  def repos = Repository.ownedBy(login)

  def homePageUrl = "/" + login
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

  def withLogin(login: String) = DAO.selectOne("SELECT  email, login, password FROM users WHERE login = ?", login) {
    row =>
      new User(row.getString("email"), row.getString("login"), row.getString("password"))
  }

  def loggedIn_? = {
    currentUserId.isDefined
  }

  def logUserIdIn(id: String) {
    curUser.remove()
    curUserId(Full(id))
  }

  val destroySessionOnLogin = true

  def logUserIn(who: User, postLogin: () => Nothing): Nothing = {
    if (destroySessionOnLogin) {
      S.session.open_!.destroySessionAndContinueInNewSession(() => {
        logUserIn(who)
        postLogin()
      })
    } else {
      logUserIn(who)
      postLogin()
    }
  }

  def logUserIn(who: User) {
    curUserId.remove()
    curUser.remove()
    curUserId(Full(who.login))
    curUser(Full(who))
  }

  def logoutCurrentUser = logUserOut()

  def logUserOut() {
    curUserId.remove()
    curUser.remove()
    S.session.foreach(_.destroySession())
  }

  private object curUserId extends SessionVar[Box[String]](Empty) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }
   /*
  private object curUserId extends SessionVar[Box[String]](Full("btd")) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }*/ //TODO только для тестов


  def currentUserId: Box[String] = curUserId.is

  private object curUser extends RequestVar[Box[User]](currentUserId.flatMap(withLogin))  with CleanRequestVarOnSessionTransition  {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  def currentUser: Box[User] = curUser.is

}



