/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import http._
import util.Helpers._
import entity.{SshKey, DAO, User}

/**
 * User: denis.bardadym
 * Date: 9/13/11
 * Time: 11:48 AM
 */

object AddNewUser {
  private var email = ""
  private var password = ""
  private var repeat_password = ""
  private var login = ""
  private var ssh_key = ""

  def render() = {
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
      "name=repeat_password" #>
        SHtml.password(repeat_password, {
          value: String =>
            repeat_password = value.trim
            if (repeat_password.isEmpty) S.error("Repeated Password field are empty")
            if (repeat_password != password) S.error("Passwords are not equal")
        }, "placeholder" -> "repeat password", "class" -> "textfield large") &
      "name=login" #>
        SHtml.password(login, {
          value: String =>
            login = value.trim
            if (login.isEmpty) S.error("Login field are empty")
        }, "placeholder" -> "login", "class" -> "textfield large") &
      "name=ssh_key" #>
        (SHtml.textarea(ssh_key, {
          value: String =>
            ssh_key = value.trim
            if (ssh_key.isEmpty) S.error("Ssh Key are empty")
        }, "placeholder" -> "Enter your ssh key",
        "class" -> "textfield",
        "cols" -> "40", "rows" -> "20") ++
            <br/>
          ++
          SHtml.button("Register", process, "class" -> "button"))
  }

  private def process() = {
    tryo {
      DAO.atomic {
        t =>
          t +: new User(email, login, password)
          t +: new SshKey(email, ssh_key)
      }
    } openOr {
      S.error("Cannot add this user")
    }
  }

}