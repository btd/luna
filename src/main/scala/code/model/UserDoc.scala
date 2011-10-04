/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb._

import common._
import mongodb.record.field.{MongoPasswordField, ObjectIdRefField, ObjectIdPk}
import util.Helpers._
import http.{CleanRequestVarOnSessionTransition, SessionVar, RequestVar, S}
import record.field._
import util._
import json.JsonDSL._
import org.bson.types.ObjectId

/**
 * User: denis.bardadym
 * Date: 9/30/11
 * Time: 4:14 PM
 */

class UserDoc private() extends MongoRecord[UserDoc] with ObjectIdPk[UserDoc] {

  object email extends StringField(this, 50) //уникальный и not null может надо будет добавить хоть какую то валидацию (н-р что там есть @)
  object login extends StringField(this, 50) //уникальный и not null
  object password extends StringField(this, 50)

  def meta = UserDoc

  def keys = SshKeyDoc.findAll("ownerId",id.is)

  def repos = RepositoryDoc.findAll("ownerId", id.is)

  def homePageUrl = "/" + login.is
}

object UserDoc extends UserDoc with MongoMetaRecord[UserDoc] {
  override def collectionName: String = "users"

def loggedIn_? = {
    currentUserId.isDefined
  }

  def logUserIdIn(id: ObjectId) {
    curUser.remove()
    curUserId(Full(id))
  }

  val destroySessionOnLogin = true

  def logUserIn(who: UserDoc, postLogin: () => Nothing): Nothing = {
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

  def logUserIn(who: UserDoc) {
    curUserId.remove()
    curUser.remove()
    curUserId(who.id.valueBox)
    curUser(Full(who))
  }

  def logoutCurrentUser = logUserOut()

  def logUserOut() {
    curUserId.remove()
    curUser.remove()
    S.session.foreach(_.destroySession())
  }

  private object curUserId extends SessionVar[Box[ObjectId]](Empty) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  /*
  private object curUserId extends SessionVar[Box[String]](Full("btd")) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }*/
  //TODO только для тестов


  def currentUserId: Box[ObjectId] = curUserId.is

  private object curUser extends RequestVar[Box[UserDoc]](tryo {UserDoc.find("_id", currentUserId.get).get } or {Empty}) with CleanRequestVarOnSessionTransition {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  def currentUser: Box[UserDoc] = curUser.is

}