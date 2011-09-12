/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import http._
import util.Helpers._
import common.Loggable
import entity.User

/**
 * User: denis.bardadym
 * Date: 9/12/11
 * Time: 3:46 PM
 */

class Login extends StatefulSnippet with Loggable {
  //private val whence = S.referer openOr "/"

  def dispatch = {
    case "render" => render
  }

  private var email = ""
  private var password = ""

  def render =
    "name=email" #> SHtml.text(email, {
      value: String =>
        email = value.trim
        if (email.isEmpty) S.error("Email field are empty")
    }, "placeholder" -> "email@example.com") &
      "name=password" #>
        SHtml.password(password, {
          value: String =>
            password = value.trim
            if (password.isEmpty) S.error("Password field are empty")
        }, "placeholder" -> "password") &
      "type=button" #> SHtml.button("Enter", process)

  private def process() = {
    User.byEmail(email) match {
      case None =>
        S.error("User with such email doesn't exists")

      case Some(u) =>
        if (u.passwd != password) S.error("Password are wrong")
        else {
          User.currentUser = Some(u)
          S.redirectTo("/")
        }

    }
  }

}