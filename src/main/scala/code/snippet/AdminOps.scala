/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb._
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
import SnippetHelper._

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 5:29 PM
 */

class AdminUserOps(up: WithUser) extends Loggable with SshKeyUI {
  private var login = ""
  private var email = ""
  private var password = ""
  

  def renderUserForm = w(up.user) { user =>
    userForm(user, updateUser(user))
  }
  

  def userForm(user: UserDoc, onSubmit: () => Unit): NodeSeq => NodeSeq = {
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
            SHtml.button("Update", onSubmit, "class" -> "button")
  }

  private def updateUser(user:UserDoc)() = {
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
        case Some(_) => {valid = false; S.error("person", "This login already used")}
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

  def renderSshKeysTable = w(up.user) {user =>
    keysTable(user.keys)
  }

 
  def renderAddKeyForm = w(up.user) {user => sshKeyForm(addNewKey(user))}
  

  private def addNewKey(user: UserDoc)() = {
    if (!ssh_key.isEmpty) {
      SshKeyDoc.createRecord.ownerId(user.id.is).rawValue(ssh_key).saveTheRecord
    }

  }

  def renderDeleteUser = w(up.user) {user => 
    "button" #> SHtml.button("Delete", processDelete(user), "class" -> "button")}

  def processDelete(user: UserDoc)() = {
      user.deleteDependend
      user.delete_!
  }

}