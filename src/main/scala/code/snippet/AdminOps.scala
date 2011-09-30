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
import entity.{DAO, User}
import code.model.SshKeyDoc

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 5:29 PM
 */

class AdminUserOps(up: UserPage) extends Loggable {
  private var login = ""
  private var email = ""
  private var password = ""
  private var ssh_key = ""

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

    up.user match {
      case Some(user) => {
        "*" #> <table class="keys_table font table">
          {user.keys.flatMap(key => {
            <tr>
              {<td>
              {key.comment}
            </td> <td>X</td>}
            </tr>
          })}
        </table>

      }
      case None => "*" #> "Invalid username"
    }

  }

  def addKey = {
    "name=ssh_key" #>
      SHtml.textarea(ssh_key, {
        value: String =>
          ssh_key = value.replaceAll("^\\s+", "")
          if (ssh_key.isEmpty) S.error("Ssh Key are empty")
      }, "placeholder" -> "Enter your ssh key",
      "class" -> "textfield",
      "cols" -> "40", "rows" -> "20") &
      "button" #> SHtml.button("Add key", addNewKey, "class" -> "button", "id" -> "add_key_button")

  }

  private def addNewKey() = {
    up.user match {
      case Some(user) => {
        SshKeyDoc.createRecord.ownerLogin(user.login).rawValue(ssh_key).save
      }
      case None => S.error("Invalid user") //TODO надо спросить у ребят как лучше такие вещи делать
    }


  }
}