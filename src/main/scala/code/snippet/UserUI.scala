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

 def userAuthForm = {
   "name=email" #> SHtml.text(email, v => email = v.trim, "placeholder" -> "email@example.com") &
   "name=password" #> SHtml.password(password, v => password = v.trim, "placeholder" -> "password") &
   "button" #> SHtml.button("Enter", logUserIn)
 }

 private def logUserIn() = {
    if (password.isEmpty) S.error("Password field is empty")

    if (email.isEmpty)
      S.error("Email field is empty")
    else
      UserDoc.find("email", email) match {
        case Full(u) if (u.password.match_?(password)) => {
          UserDoc.logUserIn(u, () => {
            S.redirectTo(u.homePageUrl)
          })
        }
        case _ =>
          S.error("User with such email doesn't exists or password is wrong")


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

  def deleteUser(user: UserDoc)() = {
      user.deleteDependend
      user.delete_!
  }
}