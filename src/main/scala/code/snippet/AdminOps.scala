/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserPage
import net.liftweb._
import common._
import util.Helpers._
import http._
import entity.{DAO}
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
      case Full(user) => {
        "name=email" #> SHtml.text(user.email.get, {
          value: String =>
            email = value.trim
            if (email.isEmpty) S.error("Email field are empty")
        },
        "placeholder" -> "email@example.com", "class" -> "textfield large") &
          "name=password" #>
            SHtml.password(user.password.get, {
              value: String =>
                password = value.trim
                if (password.isEmpty) S.error("Password field are empty")
            }, "placeholder" -> "password", "class" -> "textfield large") &
          "name=login" #>
            SHtml.text(user.login.get, {
              value: String =>
                login = value.trim
                if (login.isEmpty) S.error("Login field are empty")
            }, "placeholder" -> "login", "class" -> "textfield large") &
          "button" #>
            SHtml.button("Update", updateUser, "class" -> "button")
      }
      case _ => "*" #> "Invalid username"
    }


  }

  private def updateUser() = {
    up.user match {
      case Full(user) => {
        user.email(email).login(login).password(password).save
        S.redirectTo("/admin/" + login)
      }
      case _ => S.error("Invalid user") //TODO надо спросить у ребят как лучше такие вещи делать
    }
  }

  def keys = {

    up.user match {
      case Full(user) => {
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
      case _ => "*" #> "Invalid username"
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
      case Full(user) => {
        SshKeyDoc.createRecord.ownerId(user.id.is).rawValue(ssh_key).save
      }
      case _ => S.error("Invalid user") //TODO надо спросить у ребят как лучше такие вещи делать
    }


  }
}