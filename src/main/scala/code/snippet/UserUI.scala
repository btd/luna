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
package code.snippet

import bootstrap.liftweb._
import net.liftweb._
import http._
import util._
import js.jquery._
import JqJE._
import JqJsCmds._
import util.Helpers._
import common._
import util.PassThru
import com.foursquare.rogue.Rogue._
import xml.{NodeSeq, Text}
import code.model._
import SnippetHelper._
import org.bson.types.ObjectId

trait UserUI extends Loggable{
  private var login = ""
  private var email = ""
  private var password = ""

  def userForm(user: UserDoc): CssSel = {
    "name=email" #> SHtml.text(user.email.get, v => email = v.trim,
      "placeholder" -> "email@example.com", "class" -> "textfield large") &
      "name=password" #>
      SHtml.password("",v => password = v.trim, 
        "placeholder" -> "password", "class" -> "textfield large") &
        "name=login" #>
        SHtml.text(user.login.get, v => login = v.trim,
         "placeholder" -> "login", "class" -> "textfield large") 
       }

  def userForm(user: UserDoc, buttonText: String, onSubmit: () => Any): NodeSeq => NodeSeq = {
  	userForm(user) & button(buttonText, onSubmit)
  }

  private object whence extends RequestVar[Box[String]](S.referer)

   def userAuthForm = {
      val w = whence.get

     "name=email" #> SHtml.text(email, v => email = v.trim, "placeholder" -> "email or login") &
     "name=password" #> (SHtml.password(password, v => password = v.trim, 
              "placeholder" -> "password", "class" -> "textfield large") ++ 
            SHtml.hidden(() => whence.set(w))) &
     "button" #> SHtml.button("Enter", logUserIn)
   }



 private def logUserIn() = {
    def logIn(u: UserDoc) = {
      UserDoc.logUserIn(u, () => {
        import code.lib.Sitemap._
        //logger.debug(S.referer)
        S.redirectTo(whence openOr userRepos.calcHref(u))
      })
    }

    if (password.isEmpty) S.error("Password field is empty")

    if (email.isEmpty)
      S.error("Email/login field is empty")
    else
      UserDoc where (_.email eqs email) get  match {
        case Some(u) if u.suspended.get => S.error("User is suspended. Ask admin to unblock.")
        case Some(u) if (u.password.match_?(password)) => logIn(u)
        case _ => {
          UserDoc where (_.login eqs email) get match {
            case Some(u) if u.suspended.get => S.error("User is suspended. Ask admin to unblock.")
            case Some(u) if (u.password.match_?(password)) => logIn(u)
            case _ => S.error("User with such email or login doesn't exists or password is wrong")
          }
        }
          


      }
  }

 def fillUser(user:UserDoc) = user.email(email).login(login).password(password)

 def saveUser(user:UserDoc)():Any = {
 	saveUser(user, u => u)
 }

  def saveUser(user:UserDoc, postUpdate: (UserDoc) => Any)():Any = {

    val updated = user.email(email).login(login).password(password)

    updated.validate match {
      case Nil => {
        updated.save
        postUpdate(user)
      }
      case l => l.foreach(fe => S.error("person", fe.msg))
    }
    
  }

  def updateUser(user:UserDoc, postUpdate: (UserDoc) => Any)():Any = {
    if(user.login.get != login) user.login(login)
    if(user.email.get != email) user.email(email)
    if(!password.isEmpty) user.password(password)

    user.fields.filter(_.dirty_?).flatMap(_.validate) match {
      case Nil => { user.update; postUpdate(user) }
      case l => l.foreach(fe => S.error("person", fe.msg))
    }
    
  }

  def deleteUser(user: UserDoc)() = {
      UserDoc.logoutCurrentUser

      user.deleteDependend
      user.delete_!

      S.redirectTo("/")
  }
}