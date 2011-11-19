/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb._

import common._
import mongodb.record.field.ObjectIdPk
import util.Helpers._
import http.{CleanRequestVarOnSessionTransition, SessionVar, RequestVar, S}
import record.field._
import util._
import org.bson.types.ObjectId

import com.foursquare.rogue.Rogue._

/**
 * User: denis.bardadym
 * Date: 9/30/11
 * Time: 4:14 PM
 */

class UserDoc private() extends MongoRecord[UserDoc] with ObjectIdPk[UserDoc] {

  object email extends StringField(this, 50) {
    private def unique_?(msg: String)(value:String): List[FieldError] = {
      if ((UserDoc where (_.email eqs value) get) isDefined) 
        List(FieldError(this, msg)) 
      else 
        Nil 
    }

    override def validations = valMinLen(1, "Email cannot be empty") _ :: 
                                valRegex(".*@.*".r.pattern, "Email address must contain @") _ :: 
                                valMaxLen(maxLength, "Email cannot be more than 50 symbols") _ ::
                                unique_?("This email already used") _ :: super.validations
  }

  object login extends StringField(this, 50) {
    private def unique_?(msg: String)(value:String): List[FieldError] = {
      if ((UserDoc where (_.login eqs value) get) isDefined) 
        List(FieldError(this, msg)) 
      else 
        Nil 
    }
    override def validations = valMinLen(1, "Login cannot be empty") _ :: 
                                valRegex("""[a-zA-Z0-9\.\-]+""".r.pattern, "Login can contains only US-ASCII letters, digits, .(point), -(minus)") _ :: 
                                valMaxLen(maxLength, "Login cannot be more than 50 symbols") _ ::
                                unique_?("This login already used") _ :: super.validations
  }

  object password extends StringField(this, 500) {
    import org.mindrot.jbcrypt._

    override def validations = valMinLen(1, "Password cannot be empty") _ :: 
                                valMaxLen(maxLength, "Password cannot be more than 50 symbols") _ ::
                                 super.validations

    override def apply (in: String): UserDoc = super.apply(BCrypt.hashpw(in, BCrypt.gensalt(12)))
                                 
    def match_?(passwd: String) = BCrypt.checkpw(passwd, get)
  }

  def meta = UserDoc

  def keys = SshKeyDoc where (_.ownerId eqs id.get) fetch

  def repos = RepositoryDoc where (_.ownerId eqs id.get) fetch

  def collaboratedRepos: Seq[RepositoryDoc] = (CollaboratorDoc where (_.userId eqs id.get) fetch).map(_.repoId.obj.get)

  def homePageUrl = "/" + login.is

  def deleteDependend = {
    repos.foreach(r => {
      r.deleteDependend
      r.delete_!
    })

    CollaboratorDoc where (_.userId eqs id.get) bulkDelete_!!

    SshKeyDoc where (_.ownerId eqs id.get) bulkDelete_!!

    PullRequestDoc where (_.creatorId eqs id.get) bulkDelete_!!
  }
  
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




  def currentUserId: Box[ObjectId] = curUserId.is

  private object curUser extends RequestVar[Box[UserDoc]](tryo {
    UserDoc.find("_id", currentUserId.get).get
  } or {
    Empty
  }) with CleanRequestVarOnSessionTransition {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  def currentUser: Box[UserDoc] = curUser.is

}