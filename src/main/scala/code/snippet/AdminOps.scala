/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserPage
import net.liftweb._
import common._
import record.Field
import util.Helpers._
import http._
import code.model._
import com.foursquare.rogue.Rogue._
import js.jquery.JqJE._
import js.JsCmds._
import util._
import xml._

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
            if (email.isEmpty) S.error("person", "Email field is empty")
        },
        "placeholder" -> "email@example.com", "class" -> "textfield large") &
          "name=password" #>
            SHtml.password(user.password.get, {
              value: String =>
                password = value.trim
                if (password.isEmpty) S.error("person", "Password field is empty")
            }, "placeholder" -> "password", "class" -> "textfield large") &
          "name=login" #>
            SHtml.text(user.login.get, {
              value: String =>
                login = value.trim
                if (login.isEmpty) S.error("person", "Login field is empty")
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
        var valid = true
        if (!email.isEmpty && !login.isEmpty && !password.isEmpty) {

          if (email != user.email.get) {
            UserDoc where (_.email eqs email) get match {
              case Some(uu) =>{ valid = false; S.error("person", "This email already registered") }
              case None => user.email(email)
            }
          }
        }
        if (login != user.login.get) {
          if (!login.matches("""[a-zA-Z0-9\.\-]+""")) {valid = false; S.error("person", "Login can contains only ASCII letters, digits, .(point), -")}
          else
            UserDoc where (_.login eqs login) get match {
              case Some(uu) => {valid = false; S.error("person", "This login already used")}
              case None => user.login(login)


            }
        }
        if (password != user.password.get) {
          user.password(password)

        }
        if(valid && user.fields.contains( (f : Field[_, _]) => f.dirty_?))
          user.saveTheRecord
        S.redirectTo("/admin/" + user.login.get)


      }
      case _ => S.error("person", "Invalid user")
    }
  }

  def keys = {

    up.user match {
      case Full(user) => {
        "*" #> <table class="keys_table font table">
          {user.keys.flatMap(key => {
            <tr id={key.id.get.toString}>
              <td>
                {key.comment}
              </td>
              <td>
                {SHtml.a(Text("X")) {
                key.delete_!
                JqId(key.id.get.toString) ~> JqRemove()
              }}
              </td>
            </tr>
          })}
        </table>

      }
      case _ => PassThru
    }

  }

  def addKey = {
    "name=ssh_key" #>
      SHtml.textarea(ssh_key, {
        value: String =>
          ssh_key = value.replaceAll("^\\s+", "")
          if (ssh_key.isEmpty) S.error("keys", "Ssh Key are empty")
      }, "placeholder" -> "Enter your ssh key",
      "class" -> "textfield",
      "cols" -> "40", "rows" -> "20") &
      "button" #> SHtml.button("Add key", addNewKey, "class" -> "button", "id" -> "add_key_button")

  }

  private def addNewKey() = {
    up.user match {
      case Full(user) => {
        if (!ssh_key.isEmpty) {
          SshKeyDoc.createRecord.ownerId(user.id.is).rawValue(ssh_key).saveTheRecord
        }
      }
      case _ => S.error("keys", "Invalid user")
    }


  }

  def delete = up.user match {
    case Full(u) => "button" #> SHtml.button("Delete", processDelete, "class" -> "button")
    case _ => "*" #> NodeSeq.Empty
  }

  def processDelete() =  up.user match {
    case Full(u) => {
      u.deleteDependend
      u.delete_!
    }
    case _ => "*" #> NodeSeq.Empty
  }

}