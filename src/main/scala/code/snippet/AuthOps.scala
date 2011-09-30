package code.snippet

import net.liftweb._
import common.{Empty, Full, Loggable}
import util.Helpers._
import http._
import entity.{ DAO, User}
import util.LiftFlowOfControlException
import code.model.SshKeyDoc


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
      }
      SshKeyDoc.createRecord.ownerLogin(login).rawValue(ssh_key).save
      logger.debug("User added to DB")
      User.logUserIn(u, () => S.redirectTo(u.homePageUrl))
    } catch {
      case e: Exception if !e.isInstanceOf[LiftFlowOfControlException] => {
        logger.debug("%s %s".format(e.getClass.getName, e.getMessage))
        S.error("Cannot add this user")
      }
    }
  }

  def loginUser = {
      "name=email" #> SHtml.text(email, {
      value: String =>
        email = value.trim
        if (email.isEmpty) S.error("Email field are empty")
    },
    "placeholder" -> "email@example.com") &
      "name=password" #>
        SHtml.password(password, {
          value: String =>
            password = value.trim
            if (password.isEmpty) S.error("Password field are empty")
        }, "placeholder" -> "password") &
      "type=button" #> SHtml.button("Enter", logUserIn)


  }

  def logUserIn()= {
     User.withEmail(email) match {
      case None =>
        S.error("User with such email doesn't exists")

      case Some(u) if (u.password != password) => S.error("Password are wrong")
      case Some(u) => {
        User.logUserIn(u, () => {
          S.redirectTo(u.homePageUrl)
        })
      }

    }
  }
}