/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import common.{Box, Empty, Full, Loggable}
import http._
import util.Helpers
import util.Helpers._
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
    },
    "placeholder" -> "email@example.com") &
      "name=password" #>
        SHtml.password(password, {
          value: String =>
            password = value.trim
            if (password.isEmpty) S.error("Password field are empty")
        }, "placeholder" -> "password") &
      "type=button" #> SHtml.button("Enter", process)


  object loginRedirect extends SessionVar[Box[String]](Empty) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  def homePage = "/"

  private def process() = {
    User.withEmail(email) match {
      case None =>
        S.error("User with such email doesn't exists")

      case Some(u) if (u.password != password) => S.error("Password are wrong")
      case Some(u) => {
        val redir = loginRedirect.is match {
          case Full(url) =>
            loginRedirect(Empty)
            url
          case _ =>  homePage
        }

        User.logUserIn(u, () => {
          S.redirectTo("/" + u.login)
        })
      }

    }
  }

}