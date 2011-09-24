package code.snippet

import net.liftweb._
import common.Loggable
import util.Helpers._
import http._
import entity.{SshKey, DAO, User}
import util.LiftFlowOfControlException


/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 24.09.11
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */

object AuthOps extends Loggable{
  private var email = ""
  private var password = ""
  private var login = ""
  private var ssh_key = ""

  def newUser = {
    "name=email" #> SHtml.text(email, {
      value: String =>
        email = value.trim
        if (email.isEmpty) S.error("Email field are empty")
    },
    "placeholder" -> "email@example.com", "class" -> "textfield large") &
      "name=password" #>
        SHtml.password(password, {
          value: String =>
            password = value.trim
            if (password.isEmpty) S.error("Password field are empty")
        }, "placeholder" -> "password", "class" -> "textfield large") &
      "name=login" #>
        SHtml.text(login, {
          value: String =>
            login = value.trim
            if (login.isEmpty) S.error("Login field are empty")
        }, "placeholder" -> "login", "class" -> "textfield large") &
      "name=ssh_key" #>
        (SHtml.textarea(ssh_key, {
          value: String =>
            ssh_key = value.replaceAll("^\\s+", "")
            if (ssh_key.isEmpty) S.error("Ssh Key are empty")
        }, "placeholder" -> "Enter your ssh key",
        "class" -> "textfield",
        "cols" -> "40", "rows" -> "20") ++
            <br/>
          ++
          SHtml.button("Register", addNewUser, "class" -> "button"))
  }

  private def addNewUser() = {
    logger.debug("Trying to add new user %s %s %s with key %s".format(email, login, password, ssh_key))
    try {
      val u = new User(email, login, password)
      DAO.atomic {
        t =>
          t +: u
          t +: new SshKey(login, ssh_key)
      }
      logger.debug("User added to DB")
      User.logUserIn(u, () => S.redirectTo(u.homePageUrl))
    } catch {
      case e: Exception if !e.isInstanceOf[LiftFlowOfControlException] => {
        logger.debug("%s %s".format(e.getClass.getName, e.getMessage))
        S.error("Cannot add this user")
      }
    }
  }
}