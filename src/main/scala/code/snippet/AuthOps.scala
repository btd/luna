package code.snippet

import net.liftweb._
import common.{Full, Loggable}
import util.Helpers._
import http._
import code.model.{UserDoc, SshKeyDoc}
import com.foursquare.rogue.Rogue._

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 24.09.11
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */

class AuthOps extends Loggable {
  private var email = ""
  private var password = ""
  private var login = ""
  private var ssh_key = ""


  def newUser = {
    "name=email" #> SHtml.text(email, {
      value: String =>
        email = value.trim
        if (email.isEmpty) S.error("Email field is empty")
    },
    "placeholder" -> "email@example.com", "class" -> "textfield large") &
      "name=password" #>
        SHtml.password(password, {
          value: String =>
            password = value.trim
            if (password.isEmpty) S.error("Password field is empty")
        }, "placeholder" -> "password", "class" -> "textfield large") &
      "name=login" #>
        SHtml.text(login, {
          value: String =>
            login = value.trim
            if (login.isEmpty) S.error("Login field is empty")
        }, "placeholder" -> "login", "class" -> "textfield large") &
      "name=ssh_key" #>
        (SHtml.textarea(ssh_key, {
          value: String =>
            ssh_key = value.replaceAll("^\\s+", "")
            if (ssh_key.isEmpty) S.error("Ssh Key field is empty")
        }, "placeholder" -> "Enter your ssh key",
        "class" -> "textfield",
        "cols" -> "40", "rows" -> "20")) &
      "button" #> SHtml.button("Register", addNewUser, "class" -> "button")
  }

  private def addNewUser() = {
    logger.debug("Trying to add new user %s %s %s with key %s".format(email, login, password, ssh_key))
    if (!email.isEmpty && !login.isEmpty && !password.isEmpty && !ssh_key.isEmpty) {
      UserDoc where (_.email eqs email) get match {
        case Some(uu) => S.error("This email already registered")
        case None => {
          if (!login.matches("""[a-zA-Z0-9\.\-]+""")) S.error("Login can contains only ASCII letters, digits, .(point), -")
          else
            UserDoc where (_.login eqs login) get match {
              case Some(uu) => S.error("This login already used")
              case None => {
                val u = UserDoc.createRecord.email(email).login(login).password(password).save
                SshKeyDoc.createRecord.ownerId(u.id.is).rawValue(ssh_key).save

                logger.debug("User added to DB")
                UserDoc.logUserIn(u, () => S.redirectTo(u.homePageUrl))
              }
            }

        }
      }
    }


  }

  def loginUser = {
    "name=email" #> SHtml.text(email, {
      value: String =>
        email = value.trim

    },
    "placeholder" -> "email@example.com") &
      "name=password" #>
        SHtml.password(password, {
          value: String =>
            password = value.trim

        }, "placeholder" -> "password") &
      "type=button" #> SHtml.button("Enter", logUserIn)


  }

  def logUserIn() = {
    if (password.isEmpty) S.error("Password field is empty")

    if (email.isEmpty)
      S.error("Email field is empty")
    else
      UserDoc.find("email", email) match {
        case Full(u) if (u.password.get == password) => {
          UserDoc.logUserIn(u, () => {
            S.redirectTo(u.homePageUrl)
          })
        }
        case _ =>
          S.error("User with such email doesn't exists or password is wrong")


      }
  }
}