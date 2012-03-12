/*
   Copyright 2012 Denis Bardadym

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
                                 super.validations

    override def apply (in: String): UserDoc = super.apply(BCrypt.hashpw(in, BCrypt.gensalt(12)))
                                 
    def match_?(passwd: String) = BCrypt.checkpw(passwd, get)
  }

  def is (user: Box[UserDoc]): Boolean = user match {
    case Full(u) if(u.login.get == login.get) => true
    case _ => false
  }

  def meta = UserDoc

  def keys = SshKeyUserDoc where (_.ownerId eqs id.get) fetch

  def repos = RepositoryDoc where (_.ownerId eqs id.get) fetch

  def collaboratedRepos: Seq[RepositoryDoc] = (CollaboratorDoc where (_.userId eqs id.get) fetch).map(_.repoId.obj.get)

  def publicRepos = RepositoryDoc where (_.ownerId eqs id.get) and (_.open_? eqs true) fetch

  def privateRepos = RepositoryDoc where (_.ownerId eqs id.get) and (_.open_? eqs false) fetch

  //def homePageUrl = "/" + login.is

  def deleteDependend = {
    repos.foreach(r => {
      r.deleteDependend
      r.delete_!
    })

    CollaboratorDoc where (_.userId eqs id.get) bulkDelete_!!

    SshKeyUserDoc where (_.ownerId eqs id.get) bulkDelete_!!

    PullRequestDoc where (_.creatorId eqs id.get) bulkDelete_!!

    UserDoc.logoutCurrentUser

    S.redirectTo("/")
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

  def byName(name: String) = UserDoc where (_.login eqs name) get



  

}