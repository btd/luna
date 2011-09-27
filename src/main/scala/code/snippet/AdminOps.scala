/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserPage
import net.liftweb._
import common.Loggable
import util.Helpers._
import http._
import entity.User
import util.PassThru

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 5:29 PM
 */

class AdminUserOps(up: UserPage) extends Loggable {
  private var login = ""
  private var email = ""
  private var password = ""

  def person = {
    up.user match {
      case Some(user) => {
        "name=email" #> SHtml.text(user.email, {
          value: String =>
            email = value.trim
            if (email.isEmpty) S.error("Email field are empty")
        },
        "placeholder" -> "email@example.com", "class" -> "textfield large") &
          "name=password" #>
            SHtml.password(user.password, {
              value: String =>
                password = value.trim
                if (password.isEmpty) S.error("Password field are empty")
            }, "placeholder" -> "password", "class" -> "textfield large") &
          "name=login" #>
            SHtml.text(user.login, {
              value: String =>
                login = value.trim
                if (login.isEmpty) S.error("Login field are empty")
            }, "placeholder" -> "login", "class" -> "textfield large") &
          "button" #>
            SHtml.button("Update", updateUser, "class" -> "button")
      }
      case None => "*" #> "Invalid username"
    }


  }

  private def updateUser() = {
    up.user match {
      case Some(user) => {
        user := new User(email, login, password)
        S.redirectTo("/admin/" + login)
      }
      case None => S.error("Invalid user") //TODO надо спросить у ребят как лучше такие вещи делать
    }
  }

  def keys = {
    PassThru

  }
}