/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.RepoPage
import net.liftweb._
import http._
import js.jquery._
import JqJE._
import JqJsCmds._
import util.Helpers._
import common._
import util.PassThru
import com.foursquare.rogue.Rogue._
import xml.{NodeSeq, Text}
import code.model._
import org.bson.types.ObjectId

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 3:28 PM
 */

class AdminRepoOps(urp: RepoPage) extends Loggable {
  private var ssh_key = ""
  private var collaborator_login = ""

  private var name = ""


  def collaborators = {
    urp.repo match {
      case Full(r) => {
        "*" #>
          <table class="collaborators_table font table">
            {r.collaborators.flatMap(c => {
            <tr id={c.id.get.toString}>
              <td>
                {c.login.get}
              </td>
              <td>
                {SHtml.a(Text("X")) {
                (CollaboratorDoc where (_.userId eqs c.id.get) and (_.repoId eqs r.id.get)).findAndDeleteOne
                JqId(c.id.get.toString) ~> JqRemove()
              }}
              </td>
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
      case _ => "*" #> "Invaid repo name"
    }
  }

  def addCollaborator = {
    "name=login" #>
      SHtml.text(collaborator_login, {
        value: String =>
          collaborator_login = value.trim
          if (collaborator_login.isEmpty) S.error("collaborators", "Login field is empty")
      }, "placeholder" -> "login", "class" -> "textfield large") &
      "button" #> SHtml.button("Add collaborator", addNewCollaborator, "class" -> "button", "id" -> "add_collaborator_button")
  }


  def addKey = {
    "name=ssh_key" #>
      SHtml.textarea(ssh_key, {
        value: String =>
          ssh_key = value.replaceAll("^\\s+", "")
          if (ssh_key.isEmpty) S.error("keys", "Ssh Key field is empty")
      }, "placeholder" -> "Enter your ssh key",
      "class" -> "textfield",
      "cols" -> "40", "rows" -> "20") &
      "button" #> SHtml.button("Add key", addNewKey, "class" -> "button", "id" -> "add_key_button")

  }

  private def addNewKey() = {
    urp.repo match {
      case Full(r) => {
        if (!ssh_key.isEmpty) {
          SshKeyDoc.createRecord.ownerId(r.ownerId.is).rawValue(ssh_key).ownerRepoId(r.id.is).save
        }
      }
      case _ => S.error("Invaid repo name")
    }


  }

  private def addNewCollaborator() = {
    urp.repo match {
      case Full(r) => {
        UserDoc.find("login", collaborator_login) match {
          case Full(u) => CollaboratorDoc.createRecord.userId(u.id.get).repoId(r.id.get).save
          case _ => S.error("collaborators", "Invaid collaborator name")
        }

      }
      case _ => S.error("Invaid repo name")
    }
  }


  def repo = {
    urp.repo match {
      case Full(repo) => {
        "name=name" #> SHtml.text(repo.name.get, {
          value: String =>
            name = value.trim
            if (name.isEmpty) S.error("Name field is empty")
        },
        "placeholder" -> "Name", "class" -> "textfield large") &
          "button" #>
            SHtml.button("Update", updateRepo, "class" -> "button")
      }
      case _ => PassThru
    }


  }

  private def updateRepo() = {
    urp.repo match {
      case Full(repo) => {
        if (!name.isEmpty) {
          if (repo.owner.repos.contains((r: RepositoryDoc) => r.name.get == name)) {
            S.error("repo", "Invalid repo")
          } else {
            if (!name.matches("""[a-zA-Z0-9\.\-]+""")) {
              S.error("repo", "Repo name can contains only ASCII letters, digits, .(point), -")
            }
            else {
              repo.name(name).saveTheRecord
              S.redirectTo("/admin" + repo.homePageUrl)
            }

          }

        }
      }
      case _ => S.error("Invalid repo")
    }
  }

  def delete = urp.repo match {
    case Full(repo) => {
      "button" #> SHtml.button("Delete", processDelete, "class" -> "button")
    }
    case _ => "*" #> NodeSeq.Empty
  }

  def processDelete() = urp.repo match {
    case Full(repo) => {
      CollaboratorDoc where (_.repoId eqs repo.id.get) bulkDelete_!!

      PullRequestDoc where (_.destRepoId eqs repo.id.get) bulkDelete_!!

      PullRequestDoc where (_.srcRepoId eqs repo.id.get) bulkDelete_!!

      SshKeyDoc where (_.ownerRepoId eqs repo.id.get) bulkDelete_!!

      RepositoryDoc where (_.forkOf eqs repo.id.get) modify (_.forkOf setTo null) updateMulti

      repo.delete_!

      S.redirectTo(urp.user.get.homePageUrl)
    }
    case _ => "*" #> NodeSeq.Empty
  }
}