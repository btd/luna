/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserRepoPage
import net.liftweb._
import http._
import util.Helpers._
import common._
import entity.{User, Collaborator, SshKey, DAO}

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 3:28 PM
 */

class AdminRepoOps(urp: UserRepoPage) extends Loggable {
  private var ssh_key = ""
  private var collaborator_login = ""


  def collaborators = {
    urp.repo match {
      case Full(r) => {
        "*" #>
          <table class="collaborators_table font table">
            {r.collaborators.flatMap(c => {
            <tr>
              {<td>
              {c.login}
            </td> <td>X</td>}
            </tr>
          })}
          </table>
      }
      case _ => "*" #> "Invaid repo name"
    }
  }

  def keys = {
    urp.repo match {
      case Full(r) => {
        "*" #> <table class="keys_table font table">
          {r.keys.flatMap(key => {
            <tr>
              {<td>
              {key.comment}
            </td> <td>X</td>}
            </tr>
          })}
        </table>

      }
      case _ => "*" #> "Invaid repo name"
    }
  }
    def addCollaborator = {
         "name=login" #>
        SHtml.text(collaborator_login, {
          value: String =>
            collaborator_login = value.trim
            if (collaborator_login.isEmpty) S.error("Login field are empty")
        }, "placeholder" -> "login", "class" -> "textfield large") &
      "button" #> SHtml.button("Add collaborator", addNewCollaborator, "class" -> "button", "id" -> "add_collaborator_button")
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
    urp.repo match {
      case Full(r) => {
        r.addKey(new SshKey(r.ownerId, ssh_key, Some(r.name)))
      }
      case _ => S.error("Invaid repo name") //TODO надо спросить у ребят как лучше такие вещи делать
    }


  }

  private def addNewCollaborator() = {
     urp.repo match {
      case Full(r) => {
        User.withLogin(collaborator_login) match {
          case Some(u) => r.addCollaborator(u)
          case _ => S.error("Invaid collaborator name")
        }

      }
      case _ => S.error("Invaid repo name") //TODO надо спросить у ребят как лучше такие вещи делать
    }
  }
}